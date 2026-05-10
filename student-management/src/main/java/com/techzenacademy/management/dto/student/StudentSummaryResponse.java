package com.techzenacademy.management.dto.student;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentSummaryResponse {
    Long totalStudents;
    Long activeAccounts;
    String latestStudentName;
}
