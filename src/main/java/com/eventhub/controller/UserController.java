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

import com.eventhub.domain.dto.ApiResponseDto;
import com.eventhub.domain.dto.CreateUserRequest;
import com.eventhub.domain.dto.UserDto;
import com.eventhub.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management operations")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create a new user", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<UserDto>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("POST /users - Creating user with email: {}", request.email());

        UserDto createdUser = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDto.ok(
                        "User created successfully",
                        createdUser));
    }

    @Operation(summary = "Get user by id", description = "Returns a user using UUID identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<UserDto>> getUserById(
            @Parameter(description = "User UUID", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {

        log.info("GET /users/{} - Fetching user", userId);

        UserDto user = userService.getUserById(userId);

        return ResponseEntity
                .ok(ApiResponseDto.ok(
                        "User retrieved successfully",
                        user));
    }

    @Operation(summary = "Get all users", description = "Returns all registered users")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<UserDto>>> getAllUsers() {

        log.info("GET /users - Fetching all users");

        List<UserDto> users = userService.getAllUsers();

        return ResponseEntity
                .ok(ApiResponseDto.ok(
                        "Users retrieved successfully",
                        users));
    }

    @Operation(summary = "Get user by email", description = "Returns a user using email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponseDto<UserDto>> getUserByEmail(
            @Parameter(description = "User email", example = "user@example.com") @PathVariable String email) {

        log.info("GET /users/email/{} - Fetching user by email", email);

        UserDto user = userService.getUserByEmail(email);

        return ResponseEntity
                .ok(ApiResponseDto.ok(
                        "User retrieved successfully",
                        user));
    }

    @Operation(summary = "Update user", description = "Updates user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<UserDto>> updateUser(
            @Parameter(description = "User UUID") @PathVariable UUID userId,

            @Valid @RequestBody UserDto userDto) {

        log.info("PUT /users/{} - Updating user", userId);

        UserDto updatedUser = userService.updateUser(userId, userDto);

        return ResponseEntity
                .ok(ApiResponseDto.ok(
                        "User updated successfully",
                        updatedUser));
    }

    @Operation(summary = "Delete user", description = "Deletes a user using UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteUser(
            @Parameter(description = "User UUID") @PathVariable UUID userId) {

        log.info("DELETE /users/{} - Deleting user", userId);

        userService.deleteUser(userId);

        return ResponseEntity
                .noContent()
                .build();
    }
}