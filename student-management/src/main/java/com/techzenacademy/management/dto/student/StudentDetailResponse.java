package com.techzenacademy.management.dto.student;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentDetailResponse {
    UUID id;
    String studentCode;
    Integer enrollmentYear;
    String fullName;
    LocalDate dob;
    String phone;
    String address;
    String username;
    String status;
}
