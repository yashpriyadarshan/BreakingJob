"""
Centralised configuration: environment variables and logging setup.
"""

import os
import sys

from dotenv import load_dotenv
from loguru import logger

load_dotenv()

# ── LiveKit ────────────────────────────────────
LIVEKIT_URL        = os.getenv("LIVEKIT_URL", "wss://eddaai-jp5v1mf0.livekit.cloud")
LIVEKIT_API_KEY    = os.getenv("LIVEKIT_API_KEY")
LIVEKIT_API_SECRET = os.getenv("LIVEKIT_API_SECRET")

# ── AI Service Keys ───────────────────────────
DEEPGRAM_API_KEY   = os.getenv("DEEPGRAM_API_KEY")
GROQ_API_KEY       = os.getenv("GROQ_API_KEY")
CARTESIA_API_KEY   = os.getenv("CARTESIA_API_KEY")

# ── Azure Blob Storage ────────────────────────
AZURE_STORAGE_CONNECTION_STRING = os.getenv("AZURE_STORAGE_CONNECTION_STRING")
TRANSCRIPT_CONTAINER = "transcript-container"

# ── PostgreSQL Database ───────────────────────
_db_url_raw = os.getenv("DB_URl", "")          # e.g. host:port/dbname
_host_port, _, DB_NAME = _db_url_raw.partition("/") if "/" in _db_url_raw else (_db_url_raw, "", "eddadb")
if ":" in _host_port:
    DB_HOST, DB_PORT = _host_port.rsplit(":", 1)
else:
    DB_HOST, DB_PORT = _host_port, "6432"
DB_USERNAME = os.getenv("DB_USERNAME", "")
DB_PASSWORD = os.getenv("DB_PASSWORD", "")


def setup_logger():
    """Configure loguru with a colourful format. Call once at startup."""
    logger.remove()
    logger.add(
        sys.stderr,
        format=(
            "<green>{time:YYYY-MM-DD HH:mm:ss.SSS}</green> | "
            "<level>{level: <8}</level> | "
            "<cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> — "
            "<level>{message}</level>"
        ),
        colorize=True,
        level="DEBUG",
    )
