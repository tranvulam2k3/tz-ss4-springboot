package com.techzenacademy.management.controller;

import com.techzenacademy.management.dto.ApiResponse;
import com.techzenacademy.management.dto.user.UserCreateRequest;
import com.techzenacademy.management.dto.user.UserResponse;
import com.techzenacademy.management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User Management API")
public class UserController {
    private final UserService userService;

    /**
     * Lấy danh sách người dùng
     */
    @GetMapping
    @Operation(summary = "Get user list")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        List<UserResponse> list = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder()
                .success(true)
                .data(list)
                .build());
    }

    /**
     * Lấy chi tiết người dùng theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .data(user)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserCreateRequest req) {
        UserResponse user = userService.createUser(req);
        return ResponseEntity.status(201).body(ApiResponse.<UserResponse>builder()
                .success(true)
                .data(user)
                .build());
    }

    /**
     * Cập nhật trạng thái người dùng
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update user status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@PathVariable UUID id, @RequestBody com.techzenacademy.management.dto.user.UserStatusRequest req) {
        userService.updateStatus(id, req.getStatus());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Cập nhật trạng thái thành công")
                .build());
    }

    /**
     * Xóa người dùng
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
