package com.minimesh.greetingservice.client;

import com.minimesh.greetingservice.model.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(RestClient userServiceRestClient) {
        this.restClient = userServiceRestClient;
    }

    /**
     * Blocking call by design — greeting-service can't answer the
     * client without this data. If user-service is down or slow, this
     * will throw (RestClientResponseException / ResourceAccessException),
     * which the controller lets propagate up to a global handler.
     */
    public UserDetails fetchUser(String name) {
        return restClient.get()
                .uri("/user/{name}", name)
                .retrieve()
                .body(UserDetails.class);
    }
}
