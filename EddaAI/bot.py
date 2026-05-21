"""
Edda AI voice-bot worker.

Spawned as a subprocess by main.py for each interview session.
Connects to LiveKit, runs the Pipecat pipeline, and handles lifecycle events.
"""

import os
import sys
import argparse
import asyncio
import json

os.environ.setdefault("TRANSFORMERS_VERBOSITY", "error")

from loguru import logger

from pipecat.pipeline.pipeline import Pipeline
from pipecat.pipeline.task import PipelineTask, PipelineParams
from pipecat.pipeline.runner import PipelineRunner
from pipecat.frames.frames import EndFrame, LLMContextFrame

from pipecat.services.deepgram.stt import DeepgramSTTService
from pipecat.services.groq.llm import GroqLLMService
from pipecat.services.cartesia.tts import CartesiaTTSService

from pipecat.processors.aggregators.llm_context import LLMContext
from pipecat.processors.aggregators.llm_response_universal import (
    LLMContextAggregatorPair,
    LLMUserAggregatorParams,
)

from pipecat.audio.vad.silero import SileroVADAnalyzer
from pipecat.audio.vad.vad_analyzer import VADParams

from pipecat.transports.livekit.transport import LiveKitTransport, LiveKitParams

from config import setup_logger, DEEPGRAM_API_KEY, GROQ_API_KEY, CARTESIA_API_KEY
from prompts import build_system_prompt
from reports import generate_and_upload_reports
from processors import (
    TranscriptLogger,
    LLMResponseLogger,
    EndInterviewProcessor,
    SilenceMonitor,
)
from livekit_utils import delete_room


async def run_bot(livekit_url, token, room_name, candidate, livekit_api_key, livekit_api_secret):
    logger.info(f"[BOT] Starting Edda | candidate={candidate.get('name')!r} | room={room_name!r}")

    # ── Transport ─────────────────────────────
    transport = LiveKitTransport(
        url=livekit_url,
        token=token,
        room_name=room_name,
        params=LiveKitParams(audio_in_enabled=True, audio_out_enabled=True),
    )

    # ── AI Services ───────────────────────────
    stt = DeepgramSTTService(
        api_key=DEEPGRAM_API_KEY,
        settings=DeepgramSTTService.Settings(
            language="en-US",
            model="nova-2",
            smart_format=True,
            endpointing=300,
        ),
    )

    llm = GroqLLMService(
        api_key=GROQ_API_KEY,
        model="llama-3.1-8b-instant",
    )

    tts = CartesiaTTSService(
        api_key=CARTESIA_API_KEY,
        voice_id="79a125e8-cd45-4c13-8a67-188112f4dd22",
        output_format={
            "container": "raw",
            "encoding": "pcm_f32le",
            "sample_rate": 16000,
        },
    )

    # ── Context & Aggregators ─────────────────
    context = LLMContext(messages=[{"role": "system", "content": build_system_prompt(candidate)}])

    aggregators = LLMContextAggregatorPair(
        context,
        user_params=LLMUserAggregatorParams(
            vad_analyzer=SileroVADAnalyzer(params=VADParams(stop_secs=0.5)),
        ),
    )

    # ── Session lifecycle ─────────────────────
    shutdown_lock = asyncio.Lock()
    greeting_sent = False

    async def shutdown_session(reason: str):
        async with shutdown_lock:
            if getattr(task, "_edda_shutting_down", False):
                return
            setattr(task, "_edda_shutting_down", True)
            logger.info(f"[BOT] Shutting down session. Reason: {reason}")
            silence_monitor.stop()
            generate_and_upload_reports(context, room_name, candidate)
            try:
                await delete_room(room_name)
            except Exception as exc:
                logger.warning(f"[BOT] Note: Room deletion during shutdown: {exc}")
            await task.queue_frame(EndFrame())

    # ── Silence re-engagement ─────────────────
    async def on_silence():
        if getattr(task, "_edda_shutting_down", False):
            return
        logger.info("[BOT] Candidate silent — prompting re-engagement.")
        context.messages.append({
            "role": "user",
            "content": "(the candidate has been silent for several seconds)",
        })
        await task.queue_frames([LLMContextFrame(context)])

    silence_monitor = SilenceMonitor(timeout_secs=20, on_silence=on_silence)

    # ── Pipeline ──────────────────────────────
    pipeline = Pipeline([
        transport.input(),
        stt,
        silence_monitor,
        TranscriptLogger(),
        aggregators.user(),
        llm,
        EndInterviewProcessor(shutdown_callback=shutdown_session),
        LLMResponseLogger(),
        tts,
        transport.output(),
        aggregators.assistant(),
    ])

    task = PipelineTask(pipeline, params=PipelineParams(allow_interruptions=True))

    # ── Event Handlers ────────────────────────
    @transport.event_handler("on_participant_connected")
    async def on_participant_connected(transport_inst, participant):
        nonlocal greeting_sent
        if greeting_sent:
            return
        identity = getattr(participant, "identity", participant)
        logger.success(f"[BOT] Candidate connected: identity={identity!r}")
        await asyncio.sleep(1)
        greeting_sent = True
        await task.queue_frames([LLMContextFrame(context)])

    @transport.event_handler("on_first_participant_joined")
    async def on_first_participant_joined(transport_inst, participant_id):
        nonlocal greeting_sent
        if greeting_sent:
            return
        logger.success(f"[BOT] First participant joined: {participant_id}")
        await asyncio.sleep(1)
        greeting_sent = True
        await task.queue_frames([LLMContextFrame(context)])

    @transport.event_handler("on_participant_disconnected")
    async def on_participant_disconnected(transport_inst, participant):
        identity = getattr(participant, "identity", participant)
        if identity == "edda-bot":
            return
        await shutdown_session(f"Candidate disconnected (identity={identity!r})")

    @transport.event_handler("on_call_state_updated")
    async def on_call_state_updated(transport_inst, state):
        logger.debug(f"[BOT] Call state: {state}")

    # ── Run ────────────────────────────────────
    runner = PipelineRunner(handle_sigint=False)
    await runner.run(task)
    logger.success("[BOT] Pipeline finished. Process exiting.")


# ── CLI entry point ───────────────────────────
def parse_args():
    parser = argparse.ArgumentParser(description="Edda AI voice-bot worker")
    parser.add_argument("--token",              required=True)
    parser.add_argument("--room",               required=True)
    parser.add_argument("--livekit-url",        required=True, dest="livekit_url")
    parser.add_argument("--livekit-api-key",    required=True, dest="livekit_api_key")
    parser.add_argument("--livekit-api-secret", required=True, dest="livekit_api_secret")
    parser.add_argument("--candidate-data",     required=True, dest="candidate_data")
    return parser.parse_args()


if __name__ == "__main__":
    setup_logger()
    logger.info(f"[BOT STARTUP] sys.argv: {sys.argv}")
    args = parse_args()

    try:
        candidate = json.loads(args.candidate_data)
    except json.JSONDecodeError as exc:
        logger.critical(f"[BOT] Invalid --candidate-data JSON: {exc}")
        sys.exit(1)

    logger.info(f"[BOT] Initialising | room={args.room!r} | candidate={candidate.get('name')!r}")

    asyncio.run(run_bot(
        livekit_url=args.livekit_url,
        token=args.token,
        room_name=args.room,
        candidate=candidate,
        livekit_api_key=args.livekit_api_key,
        livekit_api_secret=args.livekit_api_secret,
    ))