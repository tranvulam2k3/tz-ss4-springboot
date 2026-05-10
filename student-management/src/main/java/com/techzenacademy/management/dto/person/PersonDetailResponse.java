package com.techzenacademy.management.dto.person;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersonDetailResponse {
    UUID id;
    String fullName;
    LocalDate dob;
    String phone;
    String contactEmail;
    String address;
}
