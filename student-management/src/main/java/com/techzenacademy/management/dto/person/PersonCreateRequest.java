package com.techzenacademy.management.dto.person;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersonCreateRequest {
    String fullName;
    LocalDate dob;
    String phone;
    String contactEmail;
    String address;
}
