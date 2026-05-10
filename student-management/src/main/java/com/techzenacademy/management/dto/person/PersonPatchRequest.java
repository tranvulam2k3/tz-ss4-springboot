//package com.techzenacademy.management.dto.person;
//
//import io.swagger.v3.oas.annotations.media.Schema;
//import org.openapitools.jackson.nullable.JsonNullable;
//
//import java.time.LocalDate;
//
//@Schema(description = "PATCH: Only include fields that need to be updated. Send 'null' to clear a field.")
//public record PersonPatchRequest(
//        @Schema(type = "string", nullable = true, description = "New full name, cannot be null")
//        JsonNullable<String> fullName,
//
//        @Schema(type = "string", format = "date", nullable = true, description = "New dob, or set null to clear")
//        JsonNullable<LocalDate> dob,
//
//        @Schema(type = "string", nullable = true, description = "New phone, or set null to clear")
//        JsonNullable<String> phone,
//
//        @Schema(type = "string", nullable = true, description = "New contact email, or set null to clear")
//        JsonNullable<String> contactEmail,
//
//        @Schema(type = "string", nullable = true, description = "New address, or set null to clear")
//        JsonNullable<String> address
//) {
//}
