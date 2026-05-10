// package com.techzenacademy.management.dto.student;
//
// import io.swagger.v3.oas.annotations.media.Schema;
// import org.openapitools.jackson.nullable.JsonNullable;
//
// @Schema(description = "PATCH: Only include fields that need to be updated.
// Send 'null' to clear a field.")
// public record StudentPatchRequest(
// @Schema(type = "string", nullable = true, description = "New student code,
// cannot be null")
// JsonNullable<String> studentCode,
//
// @Schema(type = "integer", nullable = true, description = "New enrollment
// year, or set null to clear")
// JsonNullable<Integer> enrollmentYear
// ) {
// }
