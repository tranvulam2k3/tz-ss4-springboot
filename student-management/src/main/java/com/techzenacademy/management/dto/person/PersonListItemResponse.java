package com.techzenacademy.management.dto.person;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersonListItemResponse {
    UUID id;
    String fullName;
    String contactEmail;
    Boolean isAdult;
}
