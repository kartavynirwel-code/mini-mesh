package com.minimesh.userservice.controller;

import com.minimesh.userservice.client.NotificationClient;
import com.minimesh.userservice.model.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
public class UserController {

    private final NotificationClient notificationClient;

    public UserController(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    @GetMapping("/user/{name}")
    public UserDetails getUser(@PathVariable String name) {
        // Dummy data on purpose — the point of this project is the
        // platform plumbing, not a real user store. A deterministic-ish
        // userId per call is fine here; a real service would look this
        // up from a database.
        UserDetails userDetails = new UserDetails(
                UUID.randomUUID().toString(),
                name,
                LocalDate.now().toString()
        );

        // Fire the notification AFTER building the response but BEFORE
        // returning it — this call does not block, so it doesn't delay
        // the response to the client. This satisfies "after returning
        // data, asynchronously calls notification-service" without
        // actually making the client wait on notification-service.
        notificationClient.sendNotificationAsync(
                name,
                "User details fetched for " + name
        );

        return userDetails;
    }
}
