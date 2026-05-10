package com.techzenacademy.management.controller;

import com.techzenacademy.management.dto.ApiResponse;
import com.techzenacademy.management.dto.person.PersonCreateRequest;
import com.techzenacademy.management.dto.person.PersonDetailResponse;
import com.techzenacademy.management.dto.person.PersonListItemResponse;
import com.techzenacademy.management.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/people")
@RequiredArgsConstructor
@Tag(name = "Person Management", description = "Person Management API")
public class PersonController {
    private final PersonService personService;

    /**
     * Lấy danh sách tóm tắt mọi người
     */
    @GetMapping
    @Operation(summary = "Get people list")
    public ResponseEntity<ApiResponse<List<PersonListItemResponse>>> getPeople() {
        List<PersonListItemResponse> list = personService.getAllPeople();
        return ResponseEntity.ok(ApiResponse.<List<PersonListItemResponse>>builder()
                .success(true)
                .data(list)
                .build());
    }

    /**
     * Lấy chi tiết thông tin một người theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get person by id")
    public ResponseEntity<ApiResponse<PersonDetailResponse>> getPersonById(@PathVariable UUID id) {
        PersonDetailResponse person = personService.getPersonById(id);
        return ResponseEntity.ok(ApiResponse.<PersonDetailResponse>builder()
                .success(true)
                .data(person)
                .build());
    }

    /**
     * Tạo mới một người
     */
    @PostMapping
    @Operation(summary = "Create person")
    public ResponseEntity<ApiResponse<PersonDetailResponse>> createPerson(@RequestBody PersonCreateRequest req) {
        PersonDetailResponse person = personService.createPerson(req);
        return ResponseEntity.status(201).body(ApiResponse.<PersonDetailResponse>builder()
                .success(true)
                .data(person)
                .build());
    }

    /**
     * Xóa thông tin một người
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete person")
    public ResponseEntity<ApiResponse<Void>> deletePerson(@PathVariable UUID id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
}
