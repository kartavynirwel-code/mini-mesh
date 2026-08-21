package com.minimesh.userservice.model;

/**
 * Dummy user record. A Java record instead of a classic POJO because
 * this is pure immutable data with no behavior — records skip the
 * boilerplate (getters, equals/hashCode/toString, constructor) for
 * exactly that case. Jackson serializes records to JSON automatically
 * using the component names as field names, so no extra config needed.
 */
public record UserDetails(String userId, String name, String joinedDate) {
}
