package com.minimesh.userservice.model;

/**
 * Mirrors notification-service's NotifyRequest Pydantic model exactly:
 * {"message": "...", "recipient": "..."}. Keeping the field names
 * identical across the Java/Python boundary avoids any need for custom
 * Jackson mapping and makes the contract obvious when reading both
 * codebases side by side.
 */
public record NotificationRequest(String message, String recipient) {
}
