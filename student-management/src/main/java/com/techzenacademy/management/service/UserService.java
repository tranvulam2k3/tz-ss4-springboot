package com.techzenacademy.management.service;

import com.techzenacademy.management.dto.user.UserRequest;
import com.techzenacademy.management.dto.user.UserResponse;
import com.techzenacademy.management.entity.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final List<User> users = new ArrayList<>();

    public UserService() {
        // Initialize with 5 mock data entries
        users.add(User.builder().id(UUID.randomUUID()).name("Nguyen Van A").age(20).phone("0123456789").address("Da Nang").build());
        users.add(User.builder().id(UUID.randomUUID()).name("Tran Thi B").age(22).phone("0987654321").address("Ha Noi").build());
        users.add(User.builder().id(UUID.randomUUID()).name("Le Van C").age(25).phone("0111222333").address("Ho Chi Minh").build());
        users.add(User.builder().id(UUID.randomUUID()).name("Pham Thi D").age(21).phone("0444555666").address("Hue").build());
        users.add(User.builder().id(UUID.randomUUID()).name("Hoang Van E").age(23).phone("0777888999").address("Can Tho").build());
    }

    // User user = new User("873264h34h","Nguyen Van A", )

    public List<UserResponse> findAll() {
        List<UserResponse> responses = new ArrayList<>();
        for (User user : users) {
            responses.add(mapToResponse(user));
        }
        return responses;
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .age(user.getAge())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();
    }

    public UserResponse findById(UUID id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return mapToResponse(user);
            }
        }
        return null;
    }

    public UserResponse save(UserRequest request) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .age(request.getAge())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
        users.add(user);
        return mapToResponse(user);
    }

    public UserResponse update(UUID id, UserRequest request) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(id)) {
                User updatedUser = User.builder()
                        .id(id)
                        .name(request.getName())
                        .age(request.getAge())
                        .phone(request.getPhone())
                        .address(request.getAddress())
                        .build();
                users.set(i, updatedUser);
                return mapToResponse(updatedUser);
            }
        }
        return null;
    }

    public boolean deleteById(UUID id) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(id)) {
                users.remove(i);
                return true;
            }
        }
        return false;
    }


}
