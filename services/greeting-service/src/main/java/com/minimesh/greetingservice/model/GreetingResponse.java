package com.minimesh.greetingservice.model;

/**
 * Shape returned to the frontend: {"greeting": "...", "userDetails": {...}}
 * as specified in the requirements.
 */
public record GreetingResponse(String greeting, UserDetails userDetails) {
}
