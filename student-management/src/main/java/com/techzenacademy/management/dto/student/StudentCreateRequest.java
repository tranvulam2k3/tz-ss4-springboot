package com.techzenacademy.management.dto.student;

public record StudentCreateRequest (
        String fullName,
        Integer age,
        String email
) {}
