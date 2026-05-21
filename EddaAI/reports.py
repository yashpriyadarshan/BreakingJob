"""
Post-interview report generation, Azure upload, and database persistence.

Generates three artefacts per interview:
  1. Raw Transcript JSON
  2. Candidate Feedback Report  (detailed scores /100)
  3. Recruiter Evaluation Report (detailed scores /100)
"""

import json
import datetime

from groq import Groq
from loguru import logger
from azure.storage.blob import BlobServiceClient

from config import GROQ_API_KEY, AZURE_STORAGE_CONNECTION_STRING, TRANSCRIPT_CONTAINER
from database import save_report


# ── Helpers ────────────────────────────────────

def _call_llm_json(system_prompt: str, user_prompt: str) -> dict:
    """Call Groq LLM and return parsed JSON."""
    client = Groq(api_key=GROQ_API_KEY)
    response = client.chat.completions.create(
        model="llama-3.3-70b-versatile",
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
        response_format={"type": "json_object"},
        temperature=0.3,
    )
    return json.loads(response.choices[0].message.content)


def _is_silence_message(msg: dict) -> bool:
    """Check if a message is an injected silence prompt."""
    return (
        msg.get("role") == "user"
        and "has been silent" in msg.get("content", "").lower()
    )


def _build_conversation_text(messages: list) -> str:
    """Convert context messages to a readable string, excluding silence injections."""
    lines = []
    for m in messages:
        if m.get("role") == "system" or _is_silence_message(m):
            continue
        speaker = "CANDIDATE" if m["role"] == "user" else "EDDA"
        lines.append(f"{speaker}: {m.get('content', '')}")
    return "\n".join(lines)


def _upload_to_azure(blob_name: str, content: str):
    """Upload a string to Azure Blob Storage."""
    blob_service = BlobServiceClient.from_connection_string(AZURE_STORAGE_CONNECTION_STRING)
    container = blob_service.get_container_client(TRANSCRIPT_CONTAINER)
    if not container.exists():
        container.create_container()
    container.get_blob_client(blob_name).upload_blob(content, overwrite=True)


def download_from_azure(blob_name: str) -> dict:
    """Download a JSON blob from Azure and return as dict."""
    try:
        blob_service = BlobServiceClient.from_connection_string(AZURE_STORAGE_CONNECTION_STRING)
        container = blob_service.get_container_client(TRANSCRIPT_CONTAINER)
        blob_client = container.get_blob_client(blob_name)
        download_stream = blob_client.download_blob()
        return json.loads(download_stream.readall())
    except Exception as exc:
        logger.error(f"[REPORTS] Failed to download {blob_name} from Azure: {exc}")
        return {}


# ── Transcript Builder ────────────────────────

def _build_transcript_json(messages: list, candidate: dict, room_name: str, timestamp: str) -> dict:
    role_map = {"user": "candidate", "assistant": "edda"}
    conversation = []
    for m in messages:
        if m.get("role") == "system" or _is_silence_message(m):
            continue
        conversation.append({
            "speaker": role_map.get(m["role"], m["role"]),
            "text": m.get("content", ""),
        })
    return {
        "userId": candidate.get("userId", "unknown"),
        "candidateName": candidate.get("name", "Unknown"),
        "roomName": room_name,
        "interviewDate": datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "timestamp": timestamp,
        "conversation": conversation,
    }


# ── Report Generators ─────────────────────────

def _candidate_info_block(candidate: dict) -> str:
    return (
        f"Candidate Info:\n"
        f"- userId: {candidate.get('userId', 'unknown')}\n"
        f"- Name: {candidate.get('name', 'Unknown')}\n"
        f"- Skills: {', '.join(candidate.get('skills', []))}\n"
        f"- Projects: {', '.join(candidate.get('projects', []))}\n"
        f"- Experiences: {', '.join(candidate.get('experiences', []))}\n"
    )


def _generate_candidate_report(conversation: str, candidate: dict) -> dict:
    system = (
        "You are an expert and supportive interview evaluator. "
        "Analyse the interview conversation and produce a detailed Candidate Feedback Report in JSON. "
        "Be encouraging, honest, and constructive. Provide actionable suggestions. "
        "All scores must be integers out of 100. Be fair — not overly strict, but accurate."
    )
    user = (
        f"{_candidate_info_block(candidate)}\n"
        f"Interview Conversation:\n{conversation}\n\n"
        f"Return a JSON object with EXACTLY these keys:\n"
        f'{{'
        f'"reportType": "candidate_feedback", '
        f'"userId": <userId as number or string>, '
        f'"candidateName": "...", '
        f'"interviewDate": "YYYY-MM-DD HH:MM:SS", '
        f'"overallScore": <0-100>, '
        f'"overallRating": "Excellent|Good|Average|Below Average|Poor", '
        f'"summary": "3-4 sentence overall summary", '
        f'"skillScores": [{{"skill": "...", "score": <0-100>, "feedback": "2-3 sentence detailed feedback with suggestions"}}], '
        f'"projectScores": [{{"project": "...", "score": <0-100>, "feedback": "2-3 sentence assessment of their project understanding"}}], '
        f'"experienceScore": <0-100>, '
        f'"experienceFeedback": "detailed feedback on experience depth", '
        f'"technicalDepthScore": <0-100>, '
        f'"communicationScore": <0-100>, '
        f'"problemSolvingScore": <0-100>, '
        f'"strengths": ["specific strength 1", "specific strength 2", ...], '
        f'"areasForImprovement": ["specific area 1", "specific area 2", ...], '
        f'"suggestions": ["actionable suggestion 1", "actionable suggestion 2", ...], '
        f'"recommendations": "detailed paragraph with career and learning advice"'
        f'}}'
    )
    return _call_llm_json(system, user)


def _generate_recruiter_report(conversation: str, candidate: dict) -> dict:
    system = (
        "You are an expert technical hiring evaluator. "
        "Analyse the interview conversation and produce a detailed Recruiter Evaluation Report in JSON. "
        "Be objective and data-driven. Cite specific answers as evidence. "
        "All scores must be integers out of 100. Be fair — not overly strict, but accurate."
    )
    user = (
        f"{_candidate_info_block(candidate)}\n"
        f"Interview Conversation:\n{conversation}\n\n"
        f"Return a JSON object with EXACTLY these keys:\n"
        f'{{'
        f'"reportType": "recruiter_evaluation", '
        f'"userId": <userId as number or string>, '
        f'"candidateName": "...", '
        f'"interviewDate": "YYYY-MM-DD HH:MM:SS", '
        f'"overallScore": <0-100>, '
        f'"hireRecommendation": "Strongly Recommended|Recommended|Maybe|Not Recommended", '
        f'"skillAssessment": [{{"skill": "...", "score": <0-100>, "proficiencyLevel": "Expert|Advanced|Intermediate|Beginner", "evidence": "quote or paraphrase from interview"}}], '
        f'"projectAssessment": [{{"project": "...", "score": <0-100>, "assessment": "detailed evaluation of project discussion"}}], '
        f'"experienceScore": <0-100>, '
        f'"experienceNotes": "assessment of experience depth and relevance", '
        f'"technicalDepthScore": <0-100>, '
        f'"communicationScore": <0-100>, '
        f'"problemSolvingScore": <0-100>, '
        f'"confidenceScore": <0-100>, '
        f'"redFlags": ["specific concern 1", ...], '
        f'"highlights": ["standout moment 1", ...], '
        f'"detailedNotes": "comprehensive paragraph summarising the evaluation"'
        f'}}'
    )
    return _call_llm_json(system, user)


# ── Main Orchestrator ─────────────────────────

def generate_and_upload_reports(context, room_name: str, candidate: dict):
    """
    Generate transcript + both reports, upload to Azure Blob,
    and persist everything in PostgreSQL.
    """
    messages = context.messages
    conversation = _build_conversation_text(messages)

    if not conversation.strip():
        logger.warning("[REPORTS] No conversation to analyse. Skipping.")
        return

    timestamp = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    candidate_name = candidate.get("name", "Unknown").replace(" ", "_")
    base = f"{candidate_name}_{room_name}_{timestamp}"

    transcript_data = {}
    candidate_feedback_data = {}
    recruiter_evaluation_data = {}

    transcript_blob = f"transcript_{base}.json"
    feedback_blob = f"candidate_feedback_{base}.json"
    evaluation_blob = f"recruiter_evaluation_{base}.json"

    # ── Raw Transcript ────────────────────────
    try:
        transcript_data = _build_transcript_json(messages, candidate, room_name, timestamp)
        _upload_to_azure(transcript_blob, json.dumps(transcript_data, indent=2, ensure_ascii=False))
        logger.success(f"[REPORTS] Transcript uploaded: {transcript_blob}")
    except Exception as exc:
        logger.error(f"[REPORTS] Failed to upload transcript: {exc}")

    # ── Candidate Feedback Report ─────────────
    try:
        candidate_feedback_data = _generate_candidate_report(conversation, candidate)
        _upload_to_azure(feedback_blob, json.dumps(candidate_feedback_data, indent=2, ensure_ascii=False))
        logger.success(f"[REPORTS] Candidate feedback uploaded: {feedback_blob}")
    except Exception as exc:
        logger.error(f"[REPORTS] Failed to generate/upload candidate report: {exc}")

    # ── Recruiter Evaluation Report ───────────
    try:
        recruiter_evaluation_data = _generate_recruiter_report(conversation, candidate)
        _upload_to_azure(evaluation_blob, json.dumps(recruiter_evaluation_data, indent=2, ensure_ascii=False))
        logger.success(f"[REPORTS] Recruiter evaluation uploaded: {evaluation_blob}")
    except Exception as exc:
        logger.error(f"[REPORTS] Failed to generate/upload recruiter report: {exc}")

    # ── Save to PostgreSQL ────────────────────
    try:
        user_id = int(candidate.get("userId", 0))
    except (ValueError, TypeError):
        user_id = 0

    save_report(
        user_id=user_id,
        room_name=room_name,
        candidate_name=candidate.get("name", "Unknown"),
        transcript_blob=transcript_blob,
        feedback_blob=feedback_blob,
        evaluation_blob=evaluation_blob,
    )
