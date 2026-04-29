package com.techzenacademy.management.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Student {
    UUID id;
    String fullName;
    Integer age;
    String email;

    Instant createdAt;
    Instant updatedAt;

    // === Business helpers ===
    public boolean isAdult() {
        return age != null && age >= 18;
    }

    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
