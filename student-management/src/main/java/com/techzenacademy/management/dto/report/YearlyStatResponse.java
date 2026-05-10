package com.techzenacademy.management.dto.report;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class YearlyStatResponse {
    Integer year;
    Long studentCount;
}
