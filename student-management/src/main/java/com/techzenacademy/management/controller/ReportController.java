package com.techzenacademy.management.controller;

import com.techzenacademy.management.dto.ApiResponse;
import com.techzenacademy.management.dto.report.YearlyStatResponse;
import com.techzenacademy.management.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/reports")
@RequiredArgsConstructor
@Tag(name = "Report Management", description = "Reporting and Analytics API")
public class ReportController {
    private final StudentRepository studentRepository;

    /**
     * Thống kê học viên theo năm nhập học
     */
    @GetMapping("/enrollment-stats")
    @Operation(summary = "Get enrollment statistics by year")
    public ResponseEntity<ApiResponse<List<YearlyStatResponse>>> getEnrollmentStats() {
        List<YearlyStatResponse> stats = studentRepository.getEnrollmentStats();
        return ResponseEntity.ok(ApiResponse.<List<YearlyStatResponse>>builder()
                .success(true)
                .data(stats)
                .build());
    }
}
