package com.minimesh.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * A single shared WebClient, pre-configured with the notification-service
 * base URL pulled from application.properties (which itself resolves the
 * NOTIFICATION_SERVICE_URL env var). Building it once as a bean — rather
 * than `new`-ing a client per request — lets Spring reuse the underlying
 * connection pool instead of paying connection setup cost on every call.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient notificationWebClient(
            @Value("${notification.service.url}") String notificationServiceUrl) {
        return WebClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();
    }
}
