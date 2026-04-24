# Spring Boot – Buổi 3: Mô hình MVC & CRUD Student

## 1) Mô hình MVC trong Spring Boot

### 1.1 Khái niệm tổng quan

> **MVC (Model–View–Controller)** là mô hình thiết kế phần mềm giúp tách biệt rõ ràng giữa các tầng trong ứng dụng, nhằm:
> * Dễ bảo trì, mở rộng và kiểm thử code
> * Cho phép nhiều DEV làm việc song song trên các phần khác nhau (Model, Controller, View)
> * Tăng tính tái sử dụng và giảm sự phụ thuộc giữa các thành phần

#### Trong ứng dụng Spring Boot, MVC được áp dụng mạnh mẽ trong việc xây dựng `RESTful API`, với cấu trúc phân tách như sau:


| Thành phần     | Vai trò chính                                                                                        | Trong Spring Boot                              | Ví dụ minh họa                                       |
|----------------|------------------------------------------------------------------------------------------------------|------------------------------------------------|------------------------------------------------------|
| **Model**      | Đại diện cho dữ liệu và logic nghiệp vụ                                                              | `Entity` / `Domain Object` / `DTO` / `Service` | `Student` entity biểu diễn sinh viên trong database  |
| **View**       | Hiển thị dữ liệu cho người dùng (giao diện)<br/>Trong API, View thường là JSON response thay vì HTML | `REST API` trả JSON (Spring Web)               | {"id": 1, "name": "John Doe", "age": 20}             |
| **Controller** | Xử lý request, điều phối luồng dữ liệu giữa View và Model                                            | `@RestController`                              | `StudentController` quản lý API `/api/v1/students`   |

> * Trong ứng dụng `Web MVC` truyền thống (Spring MVC + Thymeleaf), View là file .html
> * Nhưng trong `Spring Boot REST API`, View chính là `JSON` dữ liệu trả về cho client hoặc frontend

### 1.2 Flow xử lý MVC

```
Client → Controller → Service → Repository → Database
                     ↑          ↓
                     Response ← Data
```

> * `Controller` là điểm vào của mọi request. Nó nhận dữ liệu từ người dùng (qua request body, params, path, …), sau đó gọi `Service` để xử lý nghiệp vụ
> * `Service` (nằm trong tầng Model) thực hiện logic chính, có thể gọi đến `Repository` để truy xuất dữ liệu
> * `Repository` là nơi làm việc trực tiếp với Database, thường sử dụng `Spring Data JPA` (JpaRepository, CrudRepository, …)
> * Kết quả xử lý được trả ngược lại qua `Controller`, rồi gửi về client dưới dạng `JSON response` (View)

---

## 2) Dependency Injection (Spring IoC)

### 2.1 IoC (Inversion of Control)

> * Thay vì lập trình viên tự khởi tạo và quản lý object, **Spring Container** làm việc đó.
> * Các object (bean) được tạo, cấu hình, và quản lý vòng đời bởi Spring.

### 2.2 Dependency Injection

#### 2.2.1 Khái niệm

> * Dependency Injection (DI) là cơ chế Spring tự động truyền (inject) các đối tượng mà một class phụ thuộc, thay vì lập trình viên phải tự khởi tạo (new) thủ công.
> * DI giúp Spring chịu trách nhiệm quản lý vòng đời (lifecycle) và mối quan hệ phụ thuộc giữa các bean trong ứng dụng

**Ví dụ**
  * `StudentService` cần sử dụng `StudentRepository`
  * Thay vì phải viết `StudentRepository repo = new StudentRepository();` 

→ Spring sẽ tự tạo instance và inject nó vào StudentService

#### 2.2.2 Cơ chế Spring thực hiện DI

> Spring Boot có cơ chế IoC Container (Inversion of Control Container) để quản lý toàn bộ bean (đối tượng được Spring tạo ra và quản lý)
> * Khi ứng dụng khởi động, Spring quét các class có annotation như `@Component`, `@Service`, `@Repository`, `@Controller`
> * Các bean được tạo và lưu trong `ApplicationContext`
> * Khi một bean cần phụ thuộc vào bean khác, Spring **tự động inject** phụ thuộc đó

```java
@Service
public class StudentService {
  private final StudentRepository repo;

  // Constructor Injection (nên dùng)
  @Autowired
  public StudentService(StudentRepository repo) {
    this.repo = repo;
  }

  public List<Student> getAllStudents() {
    return repo.findAll();
  }
}
```

#### 2.2.3 Các cách Inject phổ biến

| Kiểu Injection        | Cách viết                                                              | Ưu điểm                                         | Hạn chế                                       |
|-----------------------|------------------------------------------------------------------------|-------------------------------------------------|-----------------------------------------------|
| Constructor Injection | Inject qua hàm khởi tạo (`@Autowired` hoặc `@RequiredArgsConstructor`) | An toàn (`final field`), dễ test, không bị null | Cách được khuyên dùng                         |
| Field Injection       | `@Autowired` trực tiếp trên thuộc tính                                 | Ngắn gọn, dễ viết                               | Khó test (không thể inject mock), không final |
| Setter Injection      | Inject qua setter method                                               | Linh hoạt, dùng khi dependency tùy chọn         | Dễ bị bỏ qua nếu không gọi setter             |


**Ví dụ từng cách**

**1. Constructor Injection (nên dùng)**

```java
@Service
@RequiredArgsConstructor // Lombok tự tạo constructor cho final fields
public class StudentService {
    private final StudentRepository repo;
}
```

> * Annotation `@RequiredArgsConstructor`: của Lombok, giúp tự tạo constructor cho final fields
> * Annotation `@Service`: của Spring, giúp Spring tạo Bean cho tầng Service
> * Annotation `@Repository`: của Spring, giúp Spring tạo Bean cho tầng Repository
> * Annotation `@Component`: của Spring, đánh dấu một class bất kỳ là Spring Bean (tổng quát)  

**2. Field Injection**

```java
@Service
public class StudentService {
  @Autowired
  private StudentRepository repo;
}
```

**3. Setter Injection**

```java
@Service
public class StudentService {
    private StudentRepository repo;

    @Autowired
    public void setRepo(StudentRepository repo) {
        this.repo = repo;
    }
}
```

#### 2.2.3 Thuật ngữ cần nhớ đối với Dependency Injection

| Thuật ngữ               | Ý nghĩa                                             |
|-------------------------|-----------------------------------------------------|
| `Bean`                  | Là object được Spring quản lý                       |
| `IoC Container`         | Là nơi Spring lưu và quản lý các bean               |
| `Dependency Injection`  | Là hành động Spring “tiêm” bean cần thiết vào class |
| `@Autowired`            | Annotation để Spring biết cần inject dependency     |
| `Constructor Injection` | Cách inject tốt nhất, nên dùng mặc định             |

---

## 3) Thực hành: CRUD cho Student (In-Memory DB)

### 3.1 Cấu trúc dự án

```
src/main/java/student/management/api_app/
 ├── controller/student/StudentController.java
 ├── service/StudentService.java
 ├── repository/StudentRepository.java
 ├── model/Student.java
 ├── dto/student/StudentCreateRequest.java
 ├── dto/student/StudentUpdateRequest.java
 └── dto/student/StudentResponse.java
```

---

### 3.2 Model và DTO

#### 3.2.1 Model: `Student`

> Vì là In-Memory, `Student` tạm thời là Domain Model thuần (chưa cần @Entity)
> * Các thuộc tính:
>   * `id` kiểu UUID, tự động tạo trong Service (in-memory)
>   * `fullName`, `dob`, `email`, `createdAt`, `updatedAt`
> * Nghiệp vụ:
>   * Tuổi học viên phải từ 16t trở lên
>   * Lưu lại thời điểm tạo mới và update học viên 

```java
// model/Student.java

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
```

> * Annotation `@Getter`: của Lombok, tự động tạo getter methods cho tất cả thuộc tính
> * Annotation `@Setter`: tương tự `@Getter` → tự tạo setter methods
> * Annotation `@NoArgsConstructor`: của Lombok, tự tạo constructor không tham số
> * Annotation `@AllArgsConstructor`: tương tự `@NoArgsConstructor` → tự tạo constructor có đủ tham số cho tất cả field
> * `Instant`: Kiểu dữ liệu thời gian, luôn ở `UTC` (múi giờ 0 - ứng với chữ Z ở cuối) → KHÔNG bị phụ thuộc vào múi giờ hệ thống

#### Bài tập 1: Viết class `Student` hoàn chỉnh

```java
// model/Student.java

// Hãy khai báo Annotation cần thiết
public class Student {
  UUID id;
  String fullName;
  Integer age;
  String email;

  Instant createdAt;
  Instant updatedAt;

  // === Business helpers ===
  public boolean isAdult() {
    // Hãy hoàn thiện code
  }

  public void onCreate() {
    // Hãy hoàn thiện code
  }

  public void onUpdate() {
    // Hãy hoàn thiện code
  }
}
```

#### 3.2.2 DTO

> * `DTO (Data Transfer Object)` là lớp dùng để nhận dữ liệu từ client (`request`) hoặc trả dữ liệu về client (`response`)
> * Nó **KHÔNG** chứa logic nghiệp vụ, chỉ có nhiệm vụ vận chuyển dữ liệu qua lại giữa client ↔ server

**1. `StudentResponse` → trả về cho client**

```java
// dto/student/StudentResponse.java

public record StudentResponse(
        UUID id,
        String fullName,
        Integer age,
        String email,

        Instant createdAt,
        Instant updatedAt,

        Boolean adult // computed field
) {}
```

**Sử dụng `record` thay cho `class`**

> * `record` là một kiểu đặc biệt của `class` được thiết kế để lưu trữ dữ liệu **bất biến** (immutable data carrier)
> * Tự đông tạo:
>   * `private final` fields
>   * `constructor`
>   * `getters`
>   * `equals()` / `hashCode()` / `toString()`
> * Vì là immutable → **KHÔNG** có setters
> * `record` chỉ nên chứa dữ liệu, không nên xử lý logic nghiệp vụ

**Nên dùng `record` ở đâu**

> Nếu `class` chỉ dùng để mang dữ liệu (data-only), hãy dùng `record` thay cho `class` để code ngắn gọn, rõ ràng và an toàn hơn

| Nơi                           | Dùng `record` | Lý do                                            |
|-------------------------------|---------------|--------------------------------------------------|
| `DTO` (`Request`/`Response`)  | Nên           | Dữ liệu chỉ mang tính truyền tải, không thay đổi |
| `Entity` hoặc `mutable class` | Không nên     | Vì không thể setter hoặc cập nhật                |

**2. `StudentCreateRequest` → POST /students**

```java
// dto/student/StudentCreateRequest.java

public record StudentCreateRequest(
        String fullName,
        Integer age,
        String email
) {}
```

**3. `StudentUpdateRequest` → PUT /students/{id}**

* Có thể chuyển sang sử dụng `record`
* Ở đây sử dụng `class` kết hợp với `lombok` để so sánh cách dùng

```java
// dto/student/StudentUpdateRequest.java

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentUpdateRequest {
  String fullName;
  Integer age;
}
```

---

### 3.3 Repository Layer (In-memory)

> * `Repository` là lớp trung gian giữa tầng `Service` và nguồn dữ liệu `database`
> * Nó chịu trách nhiệm **lưu trữ**, **truy vấn**, **cập nhật**, **xóa dữ liệu**
> * `Service` chỉ cần “gọi `Repository` để lấy hoặc lưu dữ liệu”, không cần biết dữ liệu nằm ở đâu (trong DB thật, hay chỉ là bộ nhớ tạm)

```java
// repository/StudentRepository.java

@Repository
public class StudentRepository {
    private final Map<UUID, Student> db = new HashMap<>();

    public List<Student> findAll() {
        return new ArrayList<>(db.values());
    }

    public Optional<Student> findById(UUID id) {
        return Optional.ofNullable(db.get(id));
    }

    public Student save(Student s) {
        if (s.getId() == null) s.setId(UUID.randomUUID());
        db.put(s.getId(), s);
        return s;
    }

    public void deleteById(UUID id) {
        db.remove(id);
    }
}
```

**`@Repository` là annotation của Spring framework, giúp Spring:**

> * Tự động tạo `Bean` cho class này (để có thể `@Autowired` ở `Service`)
> * Đánh dấu class này thuộc tầng `Repository`
> * Tự động quản lý ngoại lệ (`Exception Translation`) khi làm việc với DB thật (`JPA`/`Hibernate`)

**Dùng `Optional` trong `findById(UUID id)`**

> * `findById(UUID id)` Tìm học viên theo `id` → kết quả có thể `null` nếu `id` không đúng
> * Dùng kiểu `Optional` để tránh `NullPointerException` → nếu không tìm thấy thì `Optional.ofNullable(db.get(id))` trả về `Optional.empty()`
>   * Phương thức static `Optional.ofNullable(value)` → tạo ra một `Optional` từ giá trị có thể `null`
>     * Nếu `value != null` → trả về `Optional` chứa giá trị đó
>     * Nếu `value == null` → trả về `Optional.empty()` (một Optional rỗng, không có giá trị)

#### Bài tập 2: Viết class `StudentRepository` hoàn chỉnh

`repository/StudentRepository.java`

> Viết các method `CRUD` cho repo này:
> * `findAll()`
> * `findById(UUID id)`
> * `save(Student s)`
> * `deleteById(UUID id)`

---

### 3.4 Service Layer

> * `Service` là tầng trung gian giữa `Controller` và `Repository`, chịu trách nhiệm xử lý logic nghiệp vụ (business logic) của ứng dụng.
> * Trong `mô hình MVC`, `Service` đóng vai trò "bộ não", nơi diễn ra mọi quy tắc nghiệp vụ trước khi dữ liệu được lưu hoặc trả về client.
> * `Controller` chỉ tiếp nhận `request` và trả `response`, còn `Service` mới là nơi ra quyết định, tính toán, kiểm tra điều kiện, và gọi `Repository` để truy xuất hoặc lưu dữ liệu.
> * Nhờ có `Service`, mã nguồn trở nên tách biệt, dễ bảo trì, dễ kiểm thử (unit test) và có thể tái sử dụng ở nhiều nơi khác nhau (ví dụ: dùng chung cho web và mobile API).

#### Vai trò chính của Service

| Nhiệm vụ                                | Mô tả                                                                            |
|-----------------------------------------|----------------------------------------------------------------------------------|
| 1. Thực hiện nghiệp vụ (Business Logic) | Kiểm tra dữ liệu đầu vào, ràng buộc tuổi, định dạng email, ...                   |
| 2. Gọi Repository                       | Đọc/ghi dữ liệu mà không cần biết cấu trúc lưu trữ bên dưới (Map, DB, API ngoài) |
| 3. Chuyển đổi dữ liệu                   | Biến Student (`Model`) thành StudentResponse (`DTO`) trả cho client              |
| 4. Quản lý ngoại lệ nghiệp vụ           | Ném `exception` với mã lỗi phù hợp (400, 404, …)                                 |
| 5. Giữ cho Controller gọn nhẹ           | `Controller` chỉ còn nhiệm vụ định tuyến và nhận/trả dữ liệu                     |

> _**`Repository` biết “cách lưu dữ liệu”, còn `Service` biết “khi nào và vì sao phải lưu dữ liệu”**_

```java
// service/StudentService.java

@Service
@RequiredArgsConstructor
public class StudentService {

  private final StudentRepository repo;

  // === Helper: map Student → StudentResponse ===
  private StudentResponse toResponse(Student s) {
    return new StudentResponse(
            s.getId(),
            s.getFullName(),
            s.getAge(),
            s.getEmail(),
            s.getCreatedAt(),
            s.getUpdatedAt(),
            s.isAdult()
    );
  }

  public List<StudentResponse> getAllStudents() {
//    List<Student> students = repo.findAll();
//    List<StudentResponse> responses = new ArrayList<>();
//
//    for (Student student : students) {
//        StudentResponse res = toResponse(student);
//        responses.add(res);
//    }
//
//    return responses;

    // Using Stream to handle Collection data
    return repo.findAll().stream()
            .map(this::toResponse)
            .toList();
  }

  public StudentResponse getStudentById(UUID id) {
//    Optional<Student> studentOtp = repo.findById(id);
//
//    if (studentOtp.isPresent()) {
//        return toResponse(studentOtp.get());
//    } else {
//        throw new ResponseStatusException(
//                HttpStatus.NOT_FOUND, "Not found student with id: " + id);
//    }

    return repo.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Not found student with id: " + id));
  }

  public StudentResponse createStudent(StudentCreateRequest req) {
    // Business rule validation
    if (req.fullName() == null || req.fullName().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fullName is required");
    }
    if (req.age() == null || req.age() < 16) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "age must be greater than 16");
    }
    if (req.email() == null || !req.email().contains("@")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is invalid");
    }

    Student student = Student.builder()
            .fullName(req.fullName())
            .age(req.age())
            .email(req.email())
            .build();

    student.onCreate();
    return toResponse(repo.save(student));
  }

  public StudentResponse updateStudent(UUID id, StudentUpdateRequest req) {
    if (req.getFullName() == null || req.getFullName().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fullName is required");
    }
    if (req.getAge() == null || req.getAge() < 16) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "age must be greater than 16");
    }

    Student student = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Not found student with id: " + id));

    student.setFullName(req.getFullName());
    student.setAge(req.getAge());
    student.onUpdate();

    return toResponse(repo.save(student));
  }

  public void deleteStudent(UUID id) {
    if (repo.findById(id).isEmpty()) {
      throw new ResponseStatusException(
              HttpStatus.NOT_FOUND, "Not found student with id: " + id);
    }

    repo.deleteById(id);
  }
}
```

> * `Stream` là một dòng dữ liệu tuần tự cho phép xử lý từng phần tử bằng các thao tác như `map`, `filter`, `sorted`, `forEach`, `collect`, ...
>   * `students.stream()` → phương thức stream() của Collection chuyển `List<Student>` thành `Stream<Student>`
>   * `.map(this::toResponse)` → phương thức `map()` chuyển đổi từng phần tử trong `stream` từ kiểu `Student` sang kiểu `StudentResponse`
>     * `this::toResponse` là method reference, tương đương với `student -> this.toResponse(student)`
>   * `.toList()` → thu thập (collect) các phần tử trong `stream` thành một `List<StudentResponse>` mới
> * Các phương thức của Optional
>   * `map(this::toResponse)`
>     * Nếu `value == null` → trả về `Optional.empty()`
>     * Nếu `value != null` → chuyển `student` tìm thấy sang `StudentResponse`
>   * `orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found student with id: " + id))`
>     * Nếu `value != null` → trả về giá trị đó (đã được map thành StudentResponse)
>     * Nếu `value == null` → ném ResponseStatusException với mã 404 và message
>     * Cần truyền vào một `Supplier`
>       * `Supplier` là một `lambda` không nhận tham số nào, nhưng trả về một giá trị kiểu `T` (ở đây là `ResponseStatusException`)

**Note: `Biểu thức lambda` gồm 4 nhóm `functional interface` chính thuộc package `java.util.function`**

| Interface      | Input    | Output         | Mô tả                                      | Ví dụ                                                   |
|----------------|----------|----------------|--------------------------------------------|---------------------------------------------------------|
| Supplier<T>    | Không có | Trả về T       | Cung cấp giá trị (không nhận input)        | `() -> "Hello"` / <br/>`() -> new Student("Ben", 18)`   |
| Consumer<T>    | Nhận T   | void           | Tiêu thụ giá trị (chỉ thực hiện hành động) | `x -> System.out.println(x)`                            |
| Function<T, R> | Nhận T   | Trả về R       | Biến đổi từ T → R                          | `x -> x.length()` / <br/>`user -> toUserResponse(user)` |
| Predicate<T>   | Nhận T   | Trả về boolean | Kiểm tra điều kiện                         | `x -> x > 0`                                            |

#### Bài tập 3: Viết class `Student` hoàn chỉnh

`service/StudentService.java`

> Viết các method nghiệp vụ & helper cho service này:
> * Phương thức helper `toResponse(Student s)`
> * Các phương thức xử lý nghiệp vụ (xử lý ném `exception` khi cần):
>   * `getAllStudents()`
>   * `getStudentById(UUID id)`
>   * `createStudent(StudentCreateRequest req)` (có validate `fullName` không trống, `age >= 16`, `email` phải có '@')  
>   * `updateStudent(UUID id, StudentUpdateRequest req)` (có validate `fullName` không trống, `age >= 16`, `email` phải có '@')
>   * `deleteStudent(UUID id)`

---

### 3.5 Controller Layer

> * `Controller` là tầng đầu tiên trong ứng dụng Spring Boot, chịu trách nhiệm tiếp nhận và xử lý các `HTTP request` từ client (frontend, Postman, mobile app, ...)
> * Nhiệm vụ chính của `Controller` là định tuyến (routing) request đến đúng phương thức xử lý và gọi `Service` để thực hiện nghiệp vụ
> * `Controller` **KHÔNG** xử lý logic nghiệp vụ hay truy cập dữ liệu trực tiếp — nó chỉ đóng vai trò trung gian giữa người dùng và hệ thống `backend`

#### Vai trò chính của Controller

| Nhiệm vụ                           | Mô tả                                                                                                              |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| 1. Định tuyến HTTP                 | Dùng các annotation như `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` để ánh xạ URL → phương thức |
| 2. Tiếp nhận dữ liệu đầu vào       | Nhận tham số từ URL (`@PathVariable`), query (`@RequestParam`), hoặc JSON body (`@RequestBody`)                    |
| 3. Gọi Service thực hiện nghiệp vụ | Gửi dữ liệu cho `Service`, nhận kết quả trả về                                                                     |
| 4. Trả phản hồi HTTP cho client    | Dùng `ResponseEntity` để trả `JSON` cùng mã trạng thái (200, 201, 404, 204, …)                                     |
| 5. Giữ Controller “mỏng”           | Không chứa logic nghiệp vụ, chỉ làm nhiệm vụ “điều phối” dữ liệu qua lại                                           |

```java
@RestController
@RequestMapping("${api.prefix}/students")
@Tag(name = "Student Management", description = "Student Management API")
@RequiredArgsConstructor
public class StudentController {
  private final StudentService service;

  @Operation(
          summary = "Get student list",
          description = "Bài thực hành buổi 3: Thiết kế API `GET /api/v1/students` in-memory",
          responses = {
                  @io.swagger.v3.oas.annotations.responses.ApiResponse(
                          responseCode = "200",
                          description = "Success"
                  )
          }
  )
  @GetMapping
  public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudents() {
    List<StudentResponse> list = service.getAllStudents();

    ApiResponse<List<StudentResponse>> response = ApiResponse.<List<StudentResponse>>builder()
            .success(true)
            .data(list)
            .error(null)
            .build();

    return ResponseEntity.ok(response);
  }

  @Operation(
          summary = "Get student by id",
          description = "Bài thực hành buổi 3: Thiết kế API `GET /api/v1/students/id` in-memory",
          responses = {
                  @io.swagger.v3.oas.annotations.responses.ApiResponse(
                          responseCode = "200",
                          description = "Found"
                  ),
                  @io.swagger.v3.oas.annotations.responses.ApiResponse(
                          responseCode = "404",
                          description = "Not found",
                          content = @Content(schema = @Schema(implementation = ApiResponse.ApiError.class))
                  )
          }
  )
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<StudentResponse>> getById(@PathVariable UUID id) {
    StudentResponse student = service.getStudentById(id);

    ApiResponse<StudentResponse> response = ApiResponse.<StudentResponse>builder()
            .success(true).data(student).build();

    return ResponseEntity.ok(response);
  }

  @Operation(
          summary = "Create student",
          description = "Bài thực hành buổi 3: Thiết kế API `POST /api/v1/students` in-memory",
          responses = {
                  @io.swagger.v3.oas.annotations.responses.ApiResponse(
                          responseCode = "201",
                          description = "Created"
                  ),
                  @io.swagger.v3.oas.annotations.responses.ApiResponse(
                          responseCode = "400",
                          description = "Validation failed",
                          content = @Content(schema = @Schema(implementation = ApiResponse.ApiError.class))
                  )
          }
  )
  @PostMapping
  public ResponseEntity<ApiResponse<StudentResponse>> create(@RequestBody StudentCreateRequest req) {
    StudentResponse created = service.createStudent(req);

    URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

    return ResponseEntity.created(location).body(ApiResponse.<StudentResponse>builder()
            .success(true).data(created).build());
  }

  @Operation(
          summary = "Update student",
          description = "Bài thực hành buổi 3: Thiết kế API `PUT /api/v1/students/id` in-memory",
          responses = {
                  @io.swagger.v3.oas.annotations.responses.ApiResponse(
                          responseCode = "200",
                          description = "Updated"
                  ),
                  @io.swagger.v3.oas.annotations.responses.ApiResponse(
                          responseCode = "404",
                          description = "Not found",
                          content = @Content(schema = @Schema(implementation = ApiResponse.ApiError.class))
                  ),
                  @io.swagger.v3.oas.annotations.responses.ApiResponse(
                          responseCode = "400",
                          description = "Validation failed",
                          content = @Content(schema = @Schema(implementation = ApiResponse.ApiError.class))
                  )
          }
  )
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<StudentResponse>> update(
          @PathVariable UUID id,
          @RequestBody StudentUpdateRequest req
  ) {
    StudentResponse updated = service.updateStudent(id, req);

    return ResponseEntity.ok(ApiResponse.<StudentResponse>builder()
            .success(true).data(updated).build());
  }

  @Operation(
          summary = "Delete student",
          description = "Bài thực hành buổi 3: Thiết kế API `DELETE /api/v1/students/id` in-memory",
          responses = {
                  @io.swagger.v3.oas.annotations.responses.ApiResponse(
                          responseCode = "204",
                          description = "Deleted"
                  ),
                  @io.swagger.v3.oas.annotations.responses.ApiResponse(
                          responseCode = "404",
                          description = "Not found",
                          content = @Content(schema = @Schema(implementation = ApiResponse.ApiError.class))
                  )
          }
  )
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.deleteStudent(id);
    return ResponseEntity.noContent().build();
  }
}
```

> * `ApiResponse.<List<StudentResponse>>builder()`: gán kiểu `List<StudentResponse>` cho method `builder()` khi gọi
>   * Vị trí gán kiểu: phía trước method
> * API `POST` tạo mới học viên: Trả về `201 Created` + Location (`URI`) trong header 
>   * Header có thêm `Location: http://localhost:8080/api/v1/user/{id}`
>   * Client chỉ cần đọc header `Location` là biết resource mới tạo nằm ở đâu
>   * Sử dụng `ServletUriComponentsBuilder` để build URI động
>     * `.fromCurrentRequest()`: Lấy ra URL hiện tại của request thông qua `HttpServletRequest` (`http://localhost:8080/api/v1/students`)
>     * `.path("/{id}")`: Nối thêm một đoạn path mới vào cuối URL hiện tại (`http://localhost:8080/api/v1/students/{id}`)
>     * `.buildAndExpand(created.id())`: Thay thế {id} bằng giá trị UUID thực tế được lấy từ `created.id()` (`http://localhost:8080/api/v1/students/3fa85f64-5717-4562-b3fc-2c963f66afa6`)
>     * `.toUri()`: Chuyển đổi chuỗi URL trên thành đối tượng `URI` → `new URI("http://localhost:8080/api/v1/students/3fa85f64-5717-4562-b3fc-2c963f66afa6");`
> * API `DELETE`: Trả `204 No Content` → không body

#### Bài tập 4: Viết class `StudentController` hoàn chỉnh

`controller/StudentController.java`

> Viết các controller và cấu hình Swagger UI:
> * `GET /api/v1/students`: lấy danh sách tất cả học viên 
> * `GET /api/v1/students/id`: lấy học viên theo id
> * `POST /api/v1/students`: tạo mới 1 học viên
> * `PUT /api/v1/students/id`: update thông tin 1 học viên
> * `DELETE /api/v1/students/id`: xóa 1 học viên

---

## 4) Chạy thử & Kiểm tra

### 4.1 Swagger UI

* Truy cập: `http://localhost:8080/swagger-ui.html`
* Kiểm tra các endpoint CRUD:

    * `GET /api/v1/students`
    * `GET /api/v1/students/{id}`
    * `POST /api/v1/students`
    * `PUT /api/v1/students/{id}`
    * `DELETE /api/v1/students/{id}`

### 4.2 Postman

* Tạo collection “Student CRUD” và test toàn bộ API.
* Thử tạo mới, cập nhật, xóa và đọc danh sách.
