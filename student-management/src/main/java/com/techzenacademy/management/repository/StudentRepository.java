package com.techzenacademy.management.repository;

import com.techzenacademy.management.dto.report.YearlyStatResponse;
import com.techzenacademy.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    // Tìm học viên theo mã học viên
    Optional<Student> findByStudentCode(String studentCode);

    // Kiểm tra mã học viên có tồn tại
    boolean existsByStudentCode(String studentCode);

    // Tìm học viên theo năm nhập học
    List<Student> findByEnrollmentYear(Integer enrollmentYear);

    // Tìm học viên theo tên (truy vấn qua entity Person)
    List<Student> findByPerson_FullNameContainingIgnoreCase(String keyword);

    // Tìm học viên mới nhất
    Optional<Student> findTopByOrderByCreatedAtDesc();

    @Query("SELECT new com.techzenacademy.management.dto.report.YearlyStatResponse(s.enrollmentYear, COUNT(s)) " +
            "FROM Student s GROUP BY s.enrollmentYear")
    List<YearlyStatResponse> getEnrollmentStats();
}