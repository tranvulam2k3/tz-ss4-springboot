package com.techzenacademy.management.controller;

import com.techzenacademy.management.dto.ApiResponse;
import com.techzenacademy.management.dto.student.*;
import com.techzenacademy.management.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/students")
@RequiredArgsConstructor
@Tag(name = "Student Management", description = "Student Management API")
public class StudentController {
    private final StudentService service;

    /**
     * Lấy danh sách học viên
     */
    @GetMapping
    @Operation(summary = "Get student list")
    public ResponseEntity<ApiResponse<List<StudentListItemResponse>>> getStudents() {
        List<StudentListItemResponse> list = service.getAllStudents();
        return ResponseEntity.ok(ApiResponse.<List<StudentListItemResponse>>builder()
                .success(true)
                .data(list)
                .build());
    }

    /**
     * Lấy chi tiết học viên theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get student by id")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> getStudentById(@PathVariable UUID id) {
        StudentDetailResponse student = service.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.<StudentDetailResponse>builder()
                .success(true)
                .data(student)
                .build());
    }

    /**
     * Tạo mới học viên
     */
    @PostMapping
    @Operation(summary = "Create student")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> createStudent(@RequestBody StudentCreateRequest req) {
        StudentDetailResponse student = service.createStudent(req);
        return ResponseEntity.status(201)
                .body(ApiResponse.<StudentDetailResponse>builder()
                        .success(true)
                        .data(student)
                        .build());
    }

    /**
     * Cập nhật học viên
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update student")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> updateStudent(@PathVariable UUID id,
            @RequestBody StudentUpdateRequest req) {
        StudentDetailResponse student = service.updateStudent(id, req);
        return ResponseEntity.ok(ApiResponse.<StudentDetailResponse>builder()
                .success(true)
                .data(student)
                .build());
    }

    /**
     * Xóa học viên hoàn toàn (Deep Delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable UUID id) {
        service.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Báo cáo tổng hợp
     */
    @GetMapping("/summary")
    @Operation(summary = "Get student summary report")
    public ResponseEntity<ApiResponse<StudentSummaryResponse>> getSummary() {
        StudentSummaryResponse summary = service.getSummaryReport();
        return ResponseEntity.ok(ApiResponse.<StudentSummaryResponse>builder()
                .success(true)
                .data(summary)
                .build());
    }

    /**
     * Đổi mật khẩu học viên
     */
    @PostMapping("/{studentCode}/change-password")
    @Operation(summary = "Change student password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable String studentCode,
            @RequestBody ChangePasswordRequest req) {
        service.changePassword(studentCode, req);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đổi mật khẩu thành công")
                .build());
    }
}