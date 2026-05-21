"""
LiveKit helper utilities: token minting and room deletion.
"""

from livekit import api as livekit_api
from loguru import logger

from config import LIVEKIT_URL, LIVEKIT_API_KEY, LIVEKIT_API_SECRET


def mint_token(identity: str, display_name: str, room: str) -> str:
    """Create a LiveKit access token for the given identity and room."""
    token = (
        livekit_api.AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET)
        .with_identity(identity)
        .with_name(display_name)
        .with_grants(
            livekit_api.VideoGrants(
                room_join=True,
                room=room,
                can_publish=True,
                can_subscribe=True,
            )
        )
    )
    return token.to_jwt()


async def delete_room(room_name: str):
    """Delete a LiveKit room via the HTTP API."""
    http_url = LIVEKIT_URL.replace("wss://", "https://").replace("ws://", "http://")
    lkapi = livekit_api.LiveKitAPI(http_url, LIVEKIT_API_KEY, LIVEKIT_API_SECRET)
    try:
        await lkapi.room.delete_room(livekit_api.DeleteRoomRequest(room=room_name))
        logger.success(f"[BOT] LiveKit room {room_name!r} deleted.")
    finally:
        await lkapi.aclose()
