package com.minimesh.userservice.client;

import com.minimesh.userservice.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final WebClient webClient;

    public NotificationClient(WebClient notificationWebClient) {
        this.webClient = notificationWebClient;
    }

    /**
     * Fire-and-forget: kicks off the POST /notify call and returns
     * immediately. We do NOT block the caller's HTTP thread waiting for
     * notification-service to respond, because a notification event is
     * a side effect, not something the client asked for — GET /user/{name}
     * should stay fast and available even if notification-service is
     * slow or briefly down.
     *
     * .subscribe() with error/success callbacks (instead of .block())
     * is what makes this non-blocking. If this becomes .block(), the
     * calling thread stalls until notification-service replies, which
     * defeats the whole point of "asynchronously calls notification-service"
     * from the requirements.
     */
    public void sendNotificationAsync(String recipient, String message) {
        NotificationRequest request = new NotificationRequest(message, recipient);

        webClient.post()
                .uri("/notify")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        response -> log.info("Notification dispatched for recipient={}", recipient),
                        error -> log.warn(
                                "Notification dispatch failed for recipient={} (non-fatal, request already served): {}",
                                recipient, error.getMessage())
                );
    }
}
