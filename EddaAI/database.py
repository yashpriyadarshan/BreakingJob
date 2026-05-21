"""
PostgreSQL database connection and operations for interview reports.
"""

import json
import psycopg2
import psycopg2.extras
from loguru import logger

from config import DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD

_CREATE_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS interview_reports (
    id              SERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    room_name       VARCHAR(255) NOT NULL,
    candidate_name  VARCHAR(255),
    transcript_blob VARCHAR(500),
    feedback_blob   VARCHAR(500),
    evaluation_blob VARCHAR(500),
    transcript      JSONB,
    candidate_feedback JSONB,
    recruiter_evaluation JSONB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
"""


def _get_connection():
    """Create a new PostgreSQL connection."""
    return psycopg2.connect(
        host=DB_HOST,
        port=int(DB_PORT),
        dbname=DB_NAME,
        user=DB_USERNAME,
        password=DB_PASSWORD,
        sslmode="disable",
    )


def init_db():
    """Create the interview_reports table if it doesn't exist"""
    try:
        conn = _get_connection()
        with conn.cursor() as cur:
            cur.execute(_CREATE_TABLE_SQL)
        conn.commit()
        conn.close()
        logger.success("[DB] interview_reports table ready.")
    except Exception as exc:
        logger.error(f"[DB] Failed to initialise database: {exc}")


def save_report(
    user_id: int,
    room_name: str,
    candidate_name: str,
    transcript_blob: str,
    feedback_blob: str,
    evaluation_blob: str,
):
    """Insert a new interview report row."""
    sql = """
    INSERT INTO interview_reports
        (user_id, room_name, candidate_name,
         transcript_blob, feedback_blob, evaluation_blob)
    VALUES (%s, %s, %s, %s, %s, %s)
    """
    try:
        conn = _get_connection()
        with conn.cursor() as cur:
            cur.execute(sql, (
                user_id, room_name, candidate_name,
                transcript_blob, feedback_blob, evaluation_blob
            ))
        conn.commit()
        conn.close()
        logger.success(f"[DB] Report saved for user_id={user_id}, room={room_name}")
    except Exception as exc:
        logger.error(f"[DB] Failed to save report: {exc}")


def get_latest_report(user_id: int) -> dict | None:
    """Fetch the latest report for a given user_id."""
    sql = """
    SELECT transcript_blob, feedback_blob, evaluation_blob, room_name, created_at
    FROM interview_reports
    WHERE user_id = %s
    ORDER BY created_at DESC
    LIMIT 1
    """
    try:
        conn = _get_connection()
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(sql, (user_id,))
            row = cur.fetchone()
        conn.close()
        return dict(row) if row else None
    except Exception as exc:
        logger.error(f"[DB] Failed to fetch report: {exc}")
        return None


def get_all_reports(user_id: int) -> list:
    """Fetch all reports for a given user_id."""
    sql = """
    SELECT id, room_name, candidate_name, transcript_blob, feedback_blob,
           evaluation_blob, created_at
    FROM interview_reports
    WHERE user_id = %s
    ORDER BY created_at DESC
    """
    try:
        conn = _get_connection()
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(sql, (user_id,))
            rows = cur.fetchall()
        conn.close()
        return [dict(r) for r in rows]
    except Exception as exc:
        logger.error(f"[DB] Failed to fetch reports: {exc}")
        return []
