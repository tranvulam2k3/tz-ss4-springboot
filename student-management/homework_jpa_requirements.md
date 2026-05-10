# BÀI TẬP VỀ NHÀ: SPRING DATA JPA & DTO PATTERN

## MỤC TIÊU
- Thành thạo việc tạo Entity và thiết lập mối quan hệ (Relationship) trong JPA.
- Hiểu và áp dụng DTO Pattern để tùy biến dữ liệu vào (Input) và dữ liệu ra (Output) cho từng API.
- Xử lý logic nghiệp vụ liên kết nhiều bảng.

---

## PHẦN 1: THIẾT KẾ THỰC THỂ (ENTITIES)

Dựa trên cấu trúc database trong file `V1.0.0`, hãy tạo 3 Entity với các gợi ý sau:

### 1. User Entity
Ánh xạ bảng `users`. Chú ý sử dụng `@GeneratedValue` cho UUID.
```java
@Entity
@Table(name = "users")
class User {
    @Id
    UUID id;
    String username;
    String email;
    String passwordHash;
    String status; // Khuyên dùng Enum: ACTIVE, INACTIVE

    @Column(name = "created_at", insertable = false, updatable = false)
    Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    Instant updatedAt;
}
```

### 2. Person Entity
Ánh xạ bảng `people`. Thiết lập quan hệ One-to-One với `User`.
```java
@Entity
@Table(name = "people")
class Person {
    @Id
    UUID id;
    String fullName;
    LocalDate dob;
    String phone;
    String contactEmail;
    String address;

    @OneToOne
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "created_at", insertable = false, updatable = false)
    Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    Instant updatedAt;
}
```

### 3. Student Entity
Ánh xạ bảng `students`. Đặc biệt chú ý: `person_id` vừa là Primary Key vừa là Foreign Key.
```java
@Entity
@Table(name = "students")
class Student {
    @Id
    UUID personId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "person_id")
    Person person;

    String studentCode;
    Integer enrollmentYear;

    @Column(name = "created_at", insertable = false, updatable = false)
    Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    Instant updatedAt;
}
```

---

## PHẦN 2: CHI TIẾT CÁC CỤM API

Học viên cần triển khai các API theo đúng cấu trúc Request (Input) và Response (Output) dưới đây.

### 1. CỤM API HỌC VIÊN (STUDENT)

#### 1.1. Lấy danh sách học viên (GetAll)
- **Endpoint**: `GET /api/v1/students`
- **Logic**: Lấy danh sách tóm tắt tất cả học viên để hiển thị lên bảng.
- **Input**: Không có (hoặc Query params).
- **Output**: `List<StudentSearchResponse>`
```java
class StudentSearchResponse {
    UUID id;
    String studentCode;
    String fullName;
    String email;
    Integer enrollmentYear;
}
```

#### 1.2. Lấy chi tiết học viên (GetDetail)
- **Endpoint**: `GET /api/v1/students/{id}`
- **Logic**: Lấy toàn bộ thông tin chi tiết của học viên, bao gồm cả thông tin từ bảng Person và User.
- **Input**: `id` (PathVariable).
- **Output**: `StudentDetailResponse`
```java
class StudentDetailResponse {
    UUID id;
    String studentCode;
    Integer enrollmentYear;
    String fullName;
    LocalDate dob;
    String phone;
    String address;
    String username; // Lấy từ User
    String status;   // Lấy từ User
}
```

#### 1.3. Tạo mới học viên (Create)
- **Endpoint**: `POST /api/v1/students`
- **Logic**: Tạo đồng thời User, Person và Student trong một Transaction. Nếu một bước lỗi, toàn bộ phải Rollback.
- **Input**: `StudentCreateRequest`
- **Output**: `StudentDetailResponse`
```java
class StudentCreateRequest {
    String studentCode;
    Integer enrollmentYear;
    String fullName;
    LocalDate dob;
    String phone;
    String email;
    String address;
    String username;
    String password;
}
```

#### 1.4. Cập nhật học viên (Update)
- **Endpoint**: `PUT /api/v1/students/{id}`
- **Logic**: Cập nhật thông tin học tập và thông tin cá nhân. Lưu ý không cập nhật các trường nhạy cảm như username/password.
- **Input**: `StudentUpdateRequest`
- **Output**: `StudentDetailResponse`
```java
class StudentUpdateRequest {
    Integer enrollmentYear;
    String fullName;
    LocalDate dob;
    String phone;
    String email;
    String address;
}
```

---

### 2. CỤM API THÔNG TIN CÁ NHÂN (PERSON)

#### 2.1. Lấy danh sách (GetAll)
- **Endpoint**: `GET /api/v1/people`
- **Logic**: Lấy danh sách tóm tắt thông tin của mọi người trong hệ thống.
- **Output**: `List<PersonListItemResponse>`
```java
class PersonListItemResponse {
    UUID id;
    String fullName;
    String phone;
}
```

#### 2.2. Tạo mới/Cập nhật (Create/Update)
- **Endpoint**: `POST /api/v1/people` hoặc `PUT /api/v1/people/{id}`
- **Logic**: Thao tác CRUD cơ bản trên bảng `people`.
- **Input**: `PersonRequest`
- **Output**: `PersonDetailResponse`
```java
class PersonRequest {
    String fullName;
    LocalDate dob;
    String phone;
    String contactEmail;
    String address;
}

class PersonDetailResponse {
    UUID id;
    String fullName;
    LocalDate dob;
    String phone;
    String contactEmail;
    String address;
}
```

---

### 3. CỤM API TÀI KHOẢN (USER)

#### 3.1. Lấy danh sách tài khoản
- **Endpoint**: `GET /api/v1/users`
- **Logic**: Danh sách các tài khoản người dùng để quản lý trạng thái.
- **Output**: `List<UserResponse>`
```java
class UserResponse {
    UUID id;
    String username;
    String email;
    String status;
}
```

#### 3.2. Thay đổi trạng thái tài khoản
- **Endpoint**: `PATCH /api/v1/users/{id}/status`
- **Logic**: Cập nhật trạng thái `ACTIVE` hoặc `INACTIVE` cho tài khoản.
- **Input**: `UserStatusRequest`
```java
class UserStatusRequest {
    String status; // ACTIVE, INACTIVE
}
```

---

## PHẦN 3: LOGIC NÂNG CAO (COMPLEX LOGIC)

Học viên cần xử lý các nghiệp vụ liên quan đến nhiều bảng và yêu cầu tư duy logic cao hơn.

### 1. API Báo cáo tổng hợp (Summary Report)
- **Endpoint**: `GET /api/v1/students/summary`
- **Logic**: Thống kê số liệu trên toàn hệ thống.
- **Output**: `StudentSummaryResponse`
```java
class StudentSummaryResponse {
    Long totalStudents;       // Tổng số học viên
    Long activeAccounts;      // Số lượng học viên có tài khoản ACTIVE
    String latestStudentName; // Tên của học viên mới nhất được thêm vào
}
```

### 2. API Xóa học viên hoàn toàn (Deep Delete)
- **Endpoint**: `DELETE /api/v1/students/{id}`
- **Logic**: Khi xóa một học viên, hệ thống phải tự động xóa các bản ghi liên quan ở bảng `Person` và bảng `User` để làm sạch dữ liệu.
- **Yêu cầu**: Sử dụng `@Transactional` và xử lý thứ tự xóa để không vi phạm Foreign Key Constraint.

### 3. API Thống kê theo năm nhập học (Yearly Statistics)
- **Endpoint**: `GET /api/v1/reports/enrollment-stats`
- **Logic**: Group dữ liệu theo năm nhập học.
- **Output**: `List<YearlyStatResponse>`
```java
class YearlyStatResponse {
    Integer year;        // Năm nhập học
    Long studentCount;   // Số lượng học viên nhập học trong năm đó
}
```

### 4. API Đổi mật khẩu học viên (Change Password)
- **Endpoint**: `POST /api/v1/students/{studentCode}/change-password`
- **Logic**: 
    1. Tìm học viên dựa trên `studentCode`.
    2. Truy ngược ra `Person` -> `User` của học viên đó.
    3. Cập nhật `passwordHash` mới cho User.
- **Input**: `ChangePasswordRequest`
```java
class ChangePasswordRequest {
    String oldPassword;
    String newPassword;
}
```

---

## YÊU CẦU KỸ THUẬT
1. **Transaction**: API tạo Student phải đảm bảo nếu tạo User lỗi thì Person và Student không được tạo (Rollback).
2. **Validation**: Kiểm tra dữ liệu đầu vào (ví dụ: email phải đúng định dạng, phone không được để trống).
3. **Response Standard**: Tất cả API phải trả về theo format chuẩn:
```json
{
  "success": true,
  "data": { ... },
  "message": "Thành công"
}
```
