package com.techzenacademy.management.service;

import com.techzenacademy.management.dto.student.*;
import com.techzenacademy.management.entity.Person;
import com.techzenacademy.management.entity.Student;
import com.techzenacademy.management.entity.User;
import com.techzenacademy.management.mapper.StudentMapper;
import com.techzenacademy.management.repository.PersonRepository;
import com.techzenacademy.management.repository.StudentRepository;
import com.techzenacademy.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepo;
    private final PersonRepository personRepo;
    private final UserRepository userRepo;
    private final StudentMapper studentMapper;

    /**
     * Lấy danh sách tất cả học viên
     */
    public List<StudentListItemResponse> getAllStudents() {
        return studentRepo.findAll()
                .stream()
                .map(studentMapper::toListItemResponse)
                .toList();
    }

    /**
     * Lấy thông tin chi tiết học viên theo ID
     */
    public StudentDetailResponse getStudentById(UUID id) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy học viên với ID: " + id));
        return studentMapper.toDetailResponse(student);
    }

    /**
     * Tạo mới học viên (Transaction: User -> Person -> Student)
     */
    @Transactional
    public StudentDetailResponse createStudent(StudentCreateRequest req) {
        // 1. Tạo User
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash("hashed_" + req.getPassword()) // Demo logic
                .status("ACTIVE")
                .build();
        user = userRepo.save(user);

        // 2. Tạo Person gắn với User
        Person person = Person.builder()
                .fullName(req.getFullName())
                .dob(req.getDob())
                .phone(req.getPhone())
                .contactEmail(req.getEmail())
                .address(req.getAddress())
                .user(user)
                .build();
        person = personRepo.save(person);

        // 3. Tạo Student gắn với Person
        Student student = Student.builder()
                .id(person.getId())
                .person(person)
                .studentCode(req.getStudentCode())
                .enrollmentYear(req.getEnrollmentYear())
                .build();
        student = studentRepo.save(student);

        return studentMapper.toDetailResponse(student);
    }

    /**
     * Cập nhật thông tin học viên
     */
    @Transactional
    public StudentDetailResponse updateStudent(UUID id, StudentUpdateRequest req) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học viên"));

        // Cập nhật thông tin học tập
        student.setEnrollmentYear(req.getEnrollmentYear());

        // Cập nhật thông tin cá nhân
        Person person = student.getPerson();
        person.setFullName(req.getFullName());
        person.setDob(req.getDob());
        person.setPhone(req.getPhone());
        person.setContactEmail(req.getEmail());
        person.setAddress(req.getAddress());

        studentRepo.save(student);
        personRepo.save(person);

        return studentMapper.toDetailResponse(student);
    }

    /**
     * Xóa học viên hoàn toàn (Deep Delete: Student -> Person -> User)
     */
    @Transactional
    public void deleteStudent(UUID id) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học viên"));

        Person person = student.getPerson();
        User user = person.getUser();

        // Xóa theo thứ tự để tránh constraint
        studentRepo.delete(student);
        personRepo.delete(person);
        if (user != null) {
            userRepo.delete(user);
        }
    }

    /**
     * Đổi mật khẩu
     */
    @Transactional
    public void changePassword(String studentCode, ChangePasswordRequest req) {
        Student student = studentRepo.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy mã học viên"));

        User user = student.getPerson().getUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Học viên chưa có tài khoản");
        }

        // Logic kiểm tra mật khẩu cũ và hash mật khẩu mới...
        user.setPasswordHash("hashed_" + req.getNewPassword());
        userRepo.save(user);
    }

    /**
     * Báo cáo tổng hợp
     */
    public StudentSummaryResponse getSummaryReport() {
        long total = studentRepo.count();
        long active = userRepo.countByStatus("ACTIVE");
        String latestName = studentRepo.findTopByOrderByCreatedAtDesc()
                .map(s -> s.getPerson().getFullName())
                .orElse("N/A");

        return new StudentSummaryResponse(total, active, latestName);
    }
}
