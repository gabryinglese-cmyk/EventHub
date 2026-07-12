package com.eventhub.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventhub.domain.dto.ApiResponse;
import com.eventhub.domain.dto.UserDto;
import com.eventhub.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("POST /users - Creating user with email: {}", userDto.email());

        UserDto createdUser = userService.createUser(userDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User created successfully", createdUser));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID userId) {
        log.info("GET /users/{} - Fetching user", userId);

        UserDto user = userService.getUserById(userId);

        return ResponseEntity
                .ok(ApiResponse.ok("User retrieved successfully", user));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        log.info("GET /users - Fetching all users");

        List<UserDto> users = userService.getAllUsers();

        return ResponseEntity
                .ok(ApiResponse.ok("Users retrieved successfully", users));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserDto>> getUserByEmail(@PathVariable String email) {
        log.info("GET /users/email/{} - Fetching user by email", email);

        UserDto user = userService.getUserByEmail(email);

        return ResponseEntity
                .ok(ApiResponse.ok("User retrieved successfully", user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserDto userDto) {
        log.info("PUT /users/{} - Updating user", userId);

        UserDto updatedUser = userService.updateUser(userId, userDto);

        return ResponseEntity
                .ok(ApiResponse.ok("User updated successfully", updatedUser));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        log.info("DELETE /users/{} - Deleting user", userId);

        userService.deleteUser(userId);

        return ResponseEntity
                .noContent()
                .build();
    }
}