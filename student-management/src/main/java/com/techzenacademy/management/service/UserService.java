package com.techzenacademy.management.service;

import com.techzenacademy.management.dto.user.UserCreateRequest;
import com.techzenacademy.management.dto.user.UserResponse;
import com.techzenacademy.management.entity.User;
import com.techzenacademy.management.mapper.UserMapper;
import com.techzenacademy.management.repository.UserRepository;
import com.techzenacademy.management.util.NormalizerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Lấy danh sách tất cả người dùng
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    /**
     * Lấy chi tiết người dùng theo ID
     */
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với ID: " + id));
        return userMapper.toResponse(user);
    }

    /**
     * Tạo mới người dùng
     */
    public UserResponse createUser(UserCreateRequest req) {
        String username = NormalizerUtil.trimToNull(req.getUsername());
        String email = NormalizerUtil.normalizeEmail(req.getEmail());

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash("hashed_" + req.getPassword()) // Giả lập hash password
                .status("ACTIVE")
                .build();

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    /**
     * Cập nhật trạng thái người dùng
     */
    @Transactional
    public void updateStatus(UUID id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        user.setStatus(status);
        userRepository.save(user);
    }

    /**
     * Xóa người dùng theo ID
     */
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
