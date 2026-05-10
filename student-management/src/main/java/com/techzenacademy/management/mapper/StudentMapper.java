package com.techzenacademy.management.mapper;

import com.techzenacademy.management.dto.student.StudentDetailResponse;
import com.techzenacademy.management.dto.student.StudentListItemResponse;
import com.techzenacademy.management.entity.Student;
import com.techzenacademy.management.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentMapper {

    /**
     * Chuyển đổi từ Student entity sang StudentListItemResponse
     */
    public StudentListItemResponse toListItemResponse(Student s) {
        if (s == null)
            return null;
        return StudentListItemResponse.builder()
                .id(s.getId())
                .studentCode(s.getStudentCode())
                .fullName(s.getPerson().getFullName())
                .email(s.getPerson().getContactEmail())
                .enrollmentYear(s.getEnrollmentYear())
                .build();
    }

    /**
     * Chuyển đổi từ Student entity sang StudentDetailResponse
     */
    public StudentDetailResponse toDetailResponse(Student s) {
        if (s == null)
            return null;
        User user = s.getPerson().getUser();

        return StudentDetailResponse.builder()
                .id(s.getId())
                .studentCode(s.getStudentCode())
                .enrollmentYear(s.getEnrollmentYear())
                .fullName(s.getPerson().getFullName())
                .dob(s.getPerson().getDob())
                .phone(s.getPerson().getPhone())
                .address(s.getPerson().getAddress())
                .username(user != null ? user.getUsername() : null)
                .status(user != null ? user.getStatus() : null)
                .build();
    }
}
