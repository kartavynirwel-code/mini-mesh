package com.minimesh.greetingservice.controller;

import com.minimesh.greetingservice.client.UserServiceClient;
import com.minimesh.greetingservice.model.GreetingResponse;
import com.minimesh.greetingservice.model.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private final UserServiceClient userServiceClient;

    public GreetingController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @GetMapping("/hello/{name}")
    public GreetingResponse hello(@PathVariable String name) {
        UserDetails userDetails = userServiceClient.fetchUser(name);
        return new GreetingResponse("Hello, " + name + "!", userDetails);
    }
}
