# Edda AI Technical Interviewer 🎙️

An autonomous, real-time AI voice interviewer microservice built with **FastAPI**, **Pipecat-AI**, and **LiveKit** WebRTC.

## Architecture

```
POST /edda (FastAPI)
     │
     ├─ mints LiveKit JWT tokens (bot + candidate)
     ├─ spawns bot.py as a detached subprocess
     └─ returns { room_name, livekit_url, candidate_token }

bot.py (Pipecat Pipeline, independent process)
     │
     ├─ LiveKitTransport  ← WebRTC room
     ├─ DeepgramSTT       ← Speech → Text
     ├─ GroqLLM           ← Text → Text (llama-3.3-70b-versatile)
     ├─ CartesiaTTS       ← Text → Speech
     └─ SileroVAD         ← Barge-in / turn detection
```

## Setup

### 1 · Clone & install dependencies

```bash
# Using uv (recommended)
uv venv
uv pip install -r requirements.txt

# Or plain pip
pip install -r requirements.txt
```

### 2 · Configure environment

```bash
cp .env.example .env
# Edit .env and fill in your API keys
```

| Variable | Where to get it |
|---|---|
| `LIVEKIT_URL` | LiveKit Cloud dashboard → project URL |
| `LIVEKIT_API_KEY` | LiveKit Cloud → Settings → Keys |
| `LIVEKIT_API_SECRET` | LiveKit Cloud → Settings → Keys |
| `GROQ_API_KEY` | https://console.groq.com/keys |
| `DEEPGRAM_API_KEY` | https://console.deepgram.com |
| `CARTESIA_API_KEY` | https://play.cartesia.ai/keys |

### 3 · Run the API server

```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

## Usage

### Start an interview session

```bash
curl -X POST http://localhost:8000/edda \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Yash",
    "skills": ["Java", "Spring Boot", "Microservices"],
    "projects": ["BreakingJob"]
  }'
```

**Response:**
```json
{
  "room_name": "interview-yash-a1b2c3d4",
  "livekit_url": "wss://your-project.livekit.cloud",
  "candidate_token": "<candidate_jwt>"
}
```

The candidate uses `livekit_url` + `candidate_token` to join via any LiveKit-compatible client (web, mobile, or the [LiveKit Meet](https://meet.livekit.io/) demo app).

### Health check

```bash
curl http://localhost:8000/health
```

## How It Works

1. `POST /edda` arrives → `main.py` generates a unique room name, mints two JWTs, and launches `bot.py` as a fully detached OS process.
2. `bot.py` connects to the LiveKit room as **Edda** and waits.
3. When the **candidate** joins (`on_participant_connected`), Edda delivers a personalised opening greeting.
4. The Pipecat pipeline routes audio: **Candidate mic → Deepgram STT → Groq LLM → Cartesia TTS → Candidate speaker**.
5. Silero VAD enables real-time barge-in so the candidate can interrupt Edda naturally.
6. When the candidate disconnects, `on_participant_disconnected` queues an `EndFrame` for graceful shutdown.

## Cartesia Voice IDs

Replace the default voice ID in `bot.py` with any voice from your [Cartesia dashboard](https://play.cartesia.ai/):

```python
tts = CartesiaTTSService(
    api_key=...,
    voice_id="<your-preferred-voice-id>",
    ...
)
```

## File Structure

```
EddaAI/
├── main.py           # FastAPI API manager
├── bot.py            # Pipecat voice-bot worker
├── requirements.txt  # Python dependencies
├── .env.example      # Environment variable template
└── README.md
```
