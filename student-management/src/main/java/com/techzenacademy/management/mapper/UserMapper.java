package com.techzenacademy.management.mapper;

import com.techzenacademy.management.dto.user.UserResponse;
import com.techzenacademy.management.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    /**
     * Chuyển đổi từ User entity sang UserResponse DTO
     */
    public UserResponse toResponse(User u) {
        if (u == null)
            return null;
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
