package com.techzenacademy.management.service;

import com.techzenacademy.management.dto.student.StudentCreateRequest;
import com.techzenacademy.management.dto.student.StudentResponse;
import com.techzenacademy.management.dto.student.StudentUpdateRequest;
import com.techzenacademy.management.entity.Student;
import com.techzenacademy.management.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository repo;

    // === Helper: map Student → StudentResponse ===
    private StudentResponse studentToStudentResponse(Student s) {
        return new StudentResponse(
                s.getId(),
                s.getFullName(),
                s.getAge(),
                s.getEmail(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                s.isAdult());
    }

    public StudentResponse createStudent(StudentCreateRequest studentCreateRequest) {
        // Business rule validation
        if (studentCreateRequest.fullName() == null || studentCreateRequest.fullName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fullName is required");
        }
        if (studentCreateRequest.age() == null || studentCreateRequest.age() < 16) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "age must be greater than 16");
        }
        if (studentCreateRequest.email() == null || studentCreateRequest.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is invalid");
        }

        Student student = Student.builder()
                .fullName(studentCreateRequest.fullName())
                .age(studentCreateRequest.age())
                .email(studentCreateRequest.email())
                .build();

        student.onCreate();
        return studentToStudentResponse(repo.save(student));
    }

}
