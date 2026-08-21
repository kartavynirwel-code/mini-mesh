"""
notification-service
---------------------
Leaf service in the Mini-Mesh call chain. It has no outbound calls of its
own, which is why we're building it first: it lets us lock down the
request/response "contract" that user-service (its caller) will need to
match, before we write any code that depends on it.

Endpoints:
  POST /notify  -> accepts {message, recipient}, logs it, returns {status: "sent"}
  GET  /health  -> liveness/readiness probe target
"""

import logging
from datetime import datetime, timezone

from fastapi import FastAPI
from pydantic import BaseModel, Field

# --- Logging setup -----------------------------------------------------
# We configure a named logger (not the root logger) and log to stdout.
# Why: in Kubernetes, stdout/stderr is what `kubectl logs` and any log
# shipper (Fluent Bit, etc.) will pick up. There's no file to manage,
# no volume needed, and it plays nicely with 12-factor app conventions.
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(name)s | %(message)s",
)
logger = logging.getLogger("notification-service")

app = FastAPI(title="notification-service", version="1.0.0")


# --- Request/response models --------------------------------------------
# Pydantic models give us automatic request validation (FastAPI returns a
# 422 with a clear error body if the client sends a malformed payload)
# plus auto-generated OpenAPI docs at /docs. This is "free" correctness
# that we'd otherwise have to hand-write.
class NotifyRequest(BaseModel):
    message: str = Field(..., min_length=1, description="The notification body")
    recipient: str = Field(..., min_length=1, description="Who the notification is for")


class NotifyResponse(BaseModel):
    status: str


class HealthResponse(BaseModel):
    status: str


@app.post("/notify", response_model=NotifyResponse)
async def notify(payload: NotifyRequest) -> NotifyResponse:
    # In a real system this would push to SES/SNS/Twilio/etc. Here we just
    # log it — the point of this project is the platform plumbing
    # (mesh, mTLS, routing), not building a real notification pipeline.
    timestamp = datetime.now(timezone.utc).isoformat()
    logger.info(
        "Notification sent | recipient=%s | message=%s | at=%s",
        payload.recipient,
        payload.message,
        timestamp,
    )
    return NotifyResponse(status="sent")


@app.get("/health", response_model=HealthResponse)
async def health() -> HealthResponse:
    # Kept deliberately trivial: no downstream dependency checks here,
    # because notification-service doesn't call anything else. A liveness
    # probe should only fail if THIS process is broken, not because of
    # something downstream — otherwise cascading failures cause cascading
    # pod restarts, which makes an outage worse, not better.
    return HealthResponse(status="ok")


@app.get("/")
async def root():
    return {"service": "notification-service", "status": "running"}
