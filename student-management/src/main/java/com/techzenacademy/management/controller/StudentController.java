package com.techzenacademy.management.controller;

import com.techzenacademy.management.dto.ApiResponse;
import com.techzenacademy.management.dto.student.StudentCreateRequest;
import com.techzenacademy.management.dto.student.StudentResponse;
import com.techzenacademy.management.service.StudentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/students")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentController {
    StudentService studentService;

    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @RequestBody StudentCreateRequest studentCreateRequest) {
        StudentResponse studentResponse = studentService.createStudent(studentCreateRequest);
        return ResponseEntity.status(201).body(ApiResponse.<StudentResponse>builder()
                .success(true)
                .data(studentResponse)
                .build());
    }

    // Thực hiện các API GET, UPDATE, GET BY ID, DELETE
}
