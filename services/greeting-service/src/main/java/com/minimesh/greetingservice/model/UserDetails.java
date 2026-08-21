package com.minimesh.greetingservice.model;

/**
 * Mirrors user-service's UserDetails record field-for-field. We're
 * deliberately duplicating this small DTO here rather than sharing a
 * library between the two services — at this scale a shared JAR would
 * add build coupling for very little payoff. In a larger system you'd
 * weigh that trade-off against a shared contract module or a schema
 * registry.
 */
public record UserDetails(String userId, String name, String joinedDate) {
}
