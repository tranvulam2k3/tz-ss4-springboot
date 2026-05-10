package com.techzenacademy.management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    boolean success;
    String message;
    T data;
    ApiError error;

    @Builder.Default
    Instant timestamp = Instant.now();

    @Value
    @Builder
    public static class ApiError {
        String code;
        String message;
        String path;
    }
}
