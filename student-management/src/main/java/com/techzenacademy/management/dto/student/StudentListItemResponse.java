package com.techzenacademy.management.dto.student;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentListItemResponse {
    UUID id;
    String studentCode;
    String fullName;
    String email;
    Integer enrollmentYear;
}
