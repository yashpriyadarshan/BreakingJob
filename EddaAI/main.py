"""
Edda AI — FastAPI server.

Exposes the /edda endpoint that creates a LiveKit room,
mints tokens, and spawns a bot subprocess for each interview.
Also exposes GET endpoints to retrieve interview reports.
"""

import json
import sys
import uuid
import subprocess
from pathlib import Path

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List

from loguru import logger

from config import setup_logger, LIVEKIT_URL, LIVEKIT_API_KEY, LIVEKIT_API_SECRET
from livekit_utils import mint_token
from database import init_db, get_latest_report, get_all_reports
from reports import download_from_azure

# ── Logger ─────────────────────────────────────
setup_logger()

for var, name in [
    (LIVEKIT_URL, "LIVEKIT_URL"),
    (LIVEKIT_API_KEY, "LIVEKIT_API_KEY"),
    (LIVEKIT_API_SECRET, "LIVEKIT_API_SECRET"),
]:
    if not var:
        logger.warning(f"{name} is not set. Token generation will fail at runtime.")


# ── FastAPI App ────────────────────────────────
app = FastAPI(
    title="Edda AI",
    description="Autonomous real-time voice-based skill verification interview microservice.",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── Database Init ──────────────────────────────
@app.on_event("startup")
async def startup():
    init_db()


# ── Request / Response Models ──────────────────
class CandidatePayload(BaseModel):
    userId: int = Field(..., example=1)
    name: str = Field(..., example="Yash")
    skills: List[str] = Field(..., example=["Java", "Spring Boot"])
    projects: List[str] = Field(default=[], example=["BreakingJob"])
    experiences: List[str] = Field(default=[], example=["2 years at Google as SDE"])


class InterviewSession(BaseModel):
    room_name: str
    livekit_url: str
    candidate_token: str
    meeting_url: str


# ── Interview Endpoint ─────────────────────────
@app.post("/edda", response_model=InterviewSession, status_code=200)
async def start_interview(payload: CandidatePayload):
    candidate_name = payload.name.lower().replace(" ", "-")
    room_name = f"interview-{candidate_name}-{uuid.uuid4().hex[:8]}"

    logger.info(f"New interview request | candidate={payload.name!r} | room={room_name}")

    try:
        bot_token       = mint_token("edda-bot",  "Edda",       room_name)
        candidate_token = mint_token("candidate", payload.name, room_name)
    except Exception as exc:
        logger.error(f"Token generation failed: {exc}")
        raise HTTPException(status_code=500, detail=f"Token generation failed: {exc}")

    bot_script = Path(__file__).parent / "bot.py"

    try:
        log_path = Path(__file__).parent / f"bot_{room_name}.log"
        proc = subprocess.Popen(
            [
                sys.executable, str(bot_script),
                "--token",              bot_token,
                "--room",               room_name,
                "--candidate-data",     json.dumps(payload.model_dump()),
                "--livekit-url",        LIVEKIT_URL,
                "--livekit-api-key",    LIVEKIT_API_KEY,
                "--livekit-api-secret", LIVEKIT_API_SECRET,
            ],
            start_new_session=True,
            stdout=open(log_path, "w"),
            stderr=subprocess.STDOUT,
        )
        logger.info(f"Bot log: {log_path}")
        logger.success(f"Bot subprocess spawned | pid={proc.pid} | room={room_name}")
    except Exception as exc:
        logger.error(f"Failed to spawn bot subprocess: {exc}")
        raise HTTPException(status_code=500, detail=f"Bot spawn failed: {exc}")

    meeting_url = f"https://meet.livekit.io/custom?liveKitUrl={LIVEKIT_URL}&token={candidate_token}"

    return InterviewSession(
        room_name=room_name,
        livekit_url=LIVEKIT_URL,
        candidate_token=candidate_token,
        meeting_url=meeting_url,
    )


# ── Report Endpoints ──────────────────────────

@app.get("/reports/{user_id}/transcript", tags=["reports"])
async def get_transcript(user_id: int):
    """Get the latest interview transcript for a user."""
    report = get_latest_report(user_id)
    if not report or not report.get("transcript_blob"):
        raise HTTPException(status_code=404, detail="Transcript not found for this user.")
    return download_from_azure(report["transcript_blob"])


@app.get("/reports/{user_id}/candidate-feedback", tags=["reports"])
async def get_candidate_feedback(user_id: int):
    """Get the latest candidate feedback report for a user."""
    report = get_latest_report(user_id)
    if not report or not report.get("feedback_blob"):
        raise HTTPException(status_code=404, detail="Candidate feedback not found for this user.")
    return download_from_azure(report["feedback_blob"])


@app.get("/reports/{user_id}/recruiter-evaluation", tags=["reports"])
async def get_recruiter_evaluation(user_id: int):
    """Get the latest recruiter evaluation report for a user."""
    report = get_latest_report(user_id)
    if not report or not report.get("evaluation_blob"):
        raise HTTPException(status_code=404, detail="Recruiter evaluation not found for this user.")
    return download_from_azure(report["evaluation_blob"])


# ── Health ─────────────────────────────────────

@app.get("/health", tags=["ops"])
async def health():
    return {"status": "ok", "service": "Edda AI Interviewer"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)