package com.minimesh.greetingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * We use Spring's RestClient (synchronous, available in spring-web since
 * 6.1 — no need to pull in WebFlux/Reactor here) because greeting-service
 * genuinely needs to WAIT for user-service's answer before it can build
 * its combined response. That's a real contrast with user-service's
 * fire-and-forget call to notification-service: here the downstream call
 * IS the work, not a side effect.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient userServiceRestClient(@Value("${user.service.url}") String userServiceUrl) {
        return RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }
}
