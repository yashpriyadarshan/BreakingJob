"""
Custom Pipecat frame processors used in the Edda interview pipeline.
"""

import asyncio

from loguru import logger

from pipecat.frames.frames import (
    LLMContextFrame,
    TranscriptionFrame,
    TextFrame,
    LLMTextFrame,
    LLMFullResponseStartFrame,
    LLMFullResponseEndFrame,
    BotStartedSpeakingFrame,
    BotStoppedSpeakingFrame,
)
from pipecat.processors.frame_processor import FrameProcessor, FrameDirection


class TranscriptLogger(FrameProcessor):
    """Logs every STT transcription frame that flows through the pipeline."""

    async def process_frame(self, frame, direction):
        await super().process_frame(frame, direction)
        if isinstance(frame, TranscriptionFrame):
            logger.info(f"[STT] Candidate said: {frame.text!r}")
        await self.push_frame(frame, direction)


class LLMResponseLogger(FrameProcessor):
    """Logs every LLM text response that flows through the pipeline."""

    async def process_frame(self, frame, direction):
        await super().process_frame(frame, direction)
        if isinstance(frame, (TextFrame, LLMTextFrame)):
            text = getattr(frame, "text", "")
            if text:
                logger.info(f"[LLM] Edda: {text!r}")
        await self.push_frame(frame, direction)


class SilenceMonitor(FrameProcessor):
    """
    Monitors candidate silence. If no TranscriptionFrame is seen for
    `timeout_secs` seconds after the candidate has spoken at least once,
    calls `on_silence` so the bot can re-engage.
    """

    def __init__(self, timeout_secs=20, on_silence=None):
        super().__init__()
        self._timeout = timeout_secs
        self._on_silence = on_silence
        self._timer_task = None
        self._monitoring = False
        self._cooldown = False
        self._bot_speaking = False

    async def process_frame(self, frame, direction):
        await super().process_frame(frame, direction)
        
        if isinstance(frame, BotStartedSpeakingFrame):
            self._bot_speaking = True
            if self._timer_task:
                self._timer_task.cancel()
                
        elif isinstance(frame, BotStoppedSpeakingFrame):
            self._bot_speaking = False
            self._cooldown = False
            self._restart_timer()
            
        elif isinstance(frame, TranscriptionFrame) and frame.text.strip():
            self._monitoring = True
            self._cooldown = False
            self._restart_timer()
            
        await self.push_frame(frame, direction)

    def _restart_timer(self):
        if self._timer_task:
            self._timer_task.cancel()
        if self._monitoring and not self._bot_speaking:
            self._timer_task = asyncio.create_task(self._wait_for_silence())

    async def _wait_for_silence(self):
        await asyncio.sleep(self._timeout)
        if self._on_silence and self._monitoring and not self._cooldown:
            self._cooldown = True
            await self._on_silence()

    def stop(self):
        self._monitoring = False
        if self._timer_task:
            self._timer_task.cancel()


class EndInterviewProcessor(FrameProcessor):
    """
    Sits AFTER the LLM so it sees LLM text frames flowing downstream.
    Once [INTERVIEW_OVER] is detected it:
      1. Strips the tag from the current frame so TTS doesn't speak it.
      2. Drops all subsequent LLM text/audio frames (interview is over).
      3. Queues EndFrame after a short delay so TTS can finish the last sentence.
    Also blocks TranscriptionFrames flowing upstream back to the LLM once ended,
    so candidate speech after the interview is silently ignored.
    """

    def __init__(self, shutdown_callback):
        super().__init__()
        self.ended = False
        self._buffer = ""
        self._shutdown_callback = shutdown_callback

    async def process_frame(self, frame, direction):
        await super().process_frame(frame, direction)

        if self.ended:
            # Block new LLM output going to TTS/output
            if direction == FrameDirection.DOWNSTREAM and isinstance(
                frame, (TextFrame, LLMTextFrame, LLMFullResponseStartFrame, LLMFullResponseEndFrame)
            ):
                return
            # Block candidate speech going back upstream to the LLM
            if direction == FrameDirection.UPSTREAM and isinstance(
                frame, (TranscriptionFrame, LLMContextFrame)
            ):
                return

        # Detect [INTERVIEW_OVER] in downstream LLM text
        if not self.ended and direction == FrameDirection.DOWNSTREAM and isinstance(
            frame, (TextFrame, LLMTextFrame)
        ):
            text = getattr(frame, "text", "")
            self._buffer += text
            if "[INTERVIEW_OVER]" in self._buffer:
                logger.info("[BOT] [INTERVIEW_OVER] detected — closing session.")
                # Strip the tag so TTS doesn't read it aloud
                if frame.text:
                    for tag in ["[INTERVIEW_OVER]", "[INTERVIEW", "OVER]", "INTERVIEW_OVER"]:
                        frame.text = frame.text.replace(tag, "").strip()
                self.ended = True
                self._buffer = ""
                # Give TTS ~4 s to finish speaking the closing sentence, then tear down
                asyncio.create_task(self._delayed_shutdown())

        await self.push_frame(frame, direction)

    async def _delayed_shutdown(self):
        await asyncio.sleep(4)
        await self._shutdown_callback("Interview finished ([INTERVIEW_OVER] detected)")
