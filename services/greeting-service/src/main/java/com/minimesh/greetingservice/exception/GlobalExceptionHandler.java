package com.minimesh.greetingservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Without this, a user-service outage would surface to the frontend as a
 * generic Spring "Whitelabel Error Page" 500 with a stack trace. This
 * turns it into a clean 502 with a message that actually says what
 * failed — useful later when you're deliberately killing user-service
 * pods to watch how the mesh (and this error handling) behaves.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnreachable(ResourceAccessException ex) {
        log.error("user-service unreachable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "user-service is unreachable"));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, String>> handleDownstreamError(RestClientResponseException ex) {
        log.error("user-service returned an error: {} {}", ex.getRawStatusCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "user-service returned an error"));
    }
}
