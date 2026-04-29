package com.techzenacademy.management.controller;

import com.techzenacademy.management.dto.ApiResponse;
import com.techzenacademy.management.dto.user.UserRequest;
import com.techzenacademy.management.dto.user.UserResponse;
import com.techzenacademy.management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "User Management", description = "User Management API")
public class UserController {

    UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all users from mock data")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        // User user = new User("873264h34h","Nguyen Van A", )
        // ApiResponse<List<UserResponse>> respomse = new ApiResponse<List<UserResponse>>("""true",""data)
        return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder()
                .success(true)
                .data(userService.findAll())
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their UUID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.status(404).body(ApiResponse.<UserResponse>builder()
                    .success(false)
                    .error(ApiResponse.ApiError.builder()
                            .code("USER_NOT_FOUND")
                            .message("User not found with id: " + id)
                            .path("/api/v1/users/" + id)
                            .build())
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .data(user)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Add a new user to mock data")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserRequest userRequest) {
        return ResponseEntity.status(201).body(ApiResponse.<UserResponse>builder()
                .success(true)
                .data(userService.save(userRequest))
                .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user's details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable UUID id, @RequestBody UserRequest userRequest) {
        UserResponse updatedUser = userService.update(id, userRequest);
        if (updatedUser == null) {
            return ResponseEntity.status(404).body(ApiResponse.<UserResponse>builder()
                    .success(false)
                    .error(ApiResponse.ApiError.builder()
                            .code("USER_NOT_FOUND")
                            .message("User not found with id: " + id)
                            .path("/api/v1/users/" + id)
                            .build())
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .data(updatedUser)
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Remove a user from mock data")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        boolean deleted = userService.deleteById(id);
        if (!deleted) {
            return ResponseEntity.status(404).body(ApiResponse.<Void>builder()
                    .success(false)
                    .error(ApiResponse.ApiError.builder()
                            .code("USER_NOT_FOUND")
                            .message("User not found with id: " + id)
                            .path("/api/v1/users/" + id)
                            .build())
                    .build());
        }
        return ResponseEntity.status(204).body(ApiResponse.<Void>builder()
                .success(true)
                .build());
    }
}
