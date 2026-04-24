# Spring Boot – Buổi 2: Thiết kế RESTful API

## 1) REST API Best Practices

### 1.1 Naming Conventions

| Mục tiêu                                            | Quy tắc                                            |
|-----------------------------------------------------|----------------------------------------------------|
| **Danh từ cho tài nguyên**                          | `/api/v1/students`, `/api/v1/classrooms`           |
| **Sử dụng số nhiều**                                | `/students` thay vì `/student`                     |
| **Không dùng động từ trong URL**                    | Dùng HTTP method (GET, ...) thay vì `/getStudents` |
| **Quan hệ lồng nhau**                               | `/students/{id}/courses`                           |
| **Sử dụng `kebab-case` hoặc `snake_case`** cho path | `/student-grades`, `/student_records`              |

### 1.2 Versioning

> * Thêm version ngay trong URL: `/api/v1/...`
> * Giúp tránh xung đột khi thay đổi cấu trúc hoặc hành vi API

### 1.3 Status Codes – Chuẩn hóa phản hồi

| Mã                           | Ý nghĩa                                         | Sử dụng               |
|------------------------------|-------------------------------------------------|-----------------------|
| `200 OK`                     | Thành công                                      | `GET`, `PUT`, `PATCH` |
| `201 Created`                | Tạo mới thành công                              | `POST`                |
| `204 No Content`             | Xóa hoặc cập nhật thành công không trả dữ liệu  | `DELETE`              |
| `400 Bad Request`            | Dữ liệu sai định dạng hoặc validation lỗi       |                       |
| `401 Unauthorized`           | Thiếu hoặc sai token xác thực                   |                       |
| `403 Forbidden`              | Không có quyền truy cập                         |                       |
| `404 Not Found`              | Không tìm thấy tài nguyên                       |                       |
| `409 Conflict`               | Vi phạm ràng buộc (duplicate)                   |                       |
| `500 Internal Server Error`  | Lỗi không xác định phía server                  |                       |

---

## 2) JSON Response Format Chuẩn hóa

> Khi xây dựng API, tính nhất quán trong phản hồi là điều cực kỳ quan trọng.
Nếu mỗi API trả về format khác nhau, phía frontend hoặc mobile app sẽ:
> * khó parse dữ liệu
> * khó hiển thị thông báo lỗi
> * khó test và log hệ thống

> => Do đó cần xây dựng một format JSON chung cho toàn bộ API. Ví dụ:

| Trường      | Kiểu                  | Ý nghĩa                              |
|-------------|-----------------------|--------------------------------------|
| `success`   | boolean               | API thực thi thành công hay thất bại |
| `data`      | object / list / null  | Dữ liệu chính trả về khi thành công  |
| `error`     | object / null         | Thông tin lỗi chi tiết khi thất bại  |
| `timestamp` | string (ISO 8601)     | Thời điểm server phản hồi            |


### 2.1 Mẫu Response thành công

```json
{
  "success": true,
  "data": {
    "id": "1a2b3c4d",
    "fullname": "Nguyễn Văn A",
    "email": "nguyenvana@gmail.com"
  },
  "error": null,
  "timestamp": "2025-10-16T10:00:00Z"
}
```

### 2.2 Mẫu Response khi lỗi

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "STUDENT_NOT_FOUND",
    "message": "Không tìm thấy học viên",
    "path": "/api/v1/students/999"
  },
  "timestamp": "2025-10-16T10:00:00Z"
}
```

**Best practice**

* Tạo class `ApiResponse<T>` chung để dùng cho tất cả endpoint → dễ logging, monitoring và test

```java
//src/main/java/student/management/api_app/dto/ApiResponse.java

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    boolean success;
    T data; // List/Object/Null
    ApiError error;

    @Builder.Default
    Instant timestamp = Instant.now();

    // No need for ApiError if using the RFC 7807 standard (Problem Details)
    @Value
    @Builder
    public static class ApiError {
        String code;
        String message;
        String path;
    }
}
```

> * `@Data`: lombok tự động tạo `getter`, `setter`, `constructor`, `toString()`, `equals()`, `hashCode()`
> * `@Value`: khác với `@Data` là không có `setter` → class bất biến (immutable) → giúp dữ liệu không bị set ngoài ý muốn
> * `@Builder`: giúp tạo đối tượng dễ dàng bằng ApiResponse.builder()...build()
> * `@Builder.Default`: trong build(), nếu field đó chưa được set thủ công, Lombok gán giá trị mặc định vào
> * `@JsonInclude(JsonInclude.Include.NON_NULL)`: ẩn các trường null khi serialize sang JSON

_Note: Sẽ giải thích chi tiết hơn về `RFC 7807 (Problem Details)` ở bài sau_

#### Bài tập 1: Viết class `ApiResponse<T>` hoàn chỉnh

`src/main/java/student/management/api_app/dto/ApiResponse.java`

> * Gồm các field: 
>   * `boolean success`
>   * `T data`
>   * `ApiError error`
>   * `Instant timestamp` với giá trị mặc định `Instant.now()`
> * Nested class `ApiError` gồm các field:
>   * `String code`
>   * `String message`
>   * `String path`

---

## 3) Tích hợp Swagger / OpenAPI

### 3.1 Thêm dependency (Gradle)

```groovy
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13'
}
```
* Sau khi build lại dependencies, có thể xuất hiện warning ` Provides transitive vulnerable dependency maven:org.apache.commons:commons-lang3:3.17.0 CVE-2025-48924 5.3 Insufficient Information  Results powered by Mend.io `
    > * Tuy ứng dụng vẫn chạy được, nhưng có thể xuất hiện lỗi tiềm tàng trong tính năng Swagger / OpenAPI của ứng dụng 
    > * **Nguyên nhân**: `springdoc-openapi-starter-webmvc-ui:2.8.13` đã tự động pull dependency bắc cầu là `commons-lang3:3.17.0`, nhưng phiên bản này dính lỗi CVE-2025-48924
    > * Kiểm tra xem có phải `springdoc-openapi-starter-webmvc-ui:2.8.13` đã pull `commons-lang3` về
    >   * Trên IntelliJ mở `View` → `Tool Windows` → `Problems`
    >   * Trong panel `Problems` → `Vulnerable Dependencies` → package `student_management:main` → hiển thị cây dependencies chứa `commons-lang3`
    >   * Chọn `maven:org.apache.commons:commons-lang3:3.17.0` để xem thông tin về lỗi CVE-2025-48924
    > * **Cách giải quyết**: Cần thêm dependency `commons-lang3` thủ công với phiên bản an toàn `3.18.0`

```groovy
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13'
    implementation 'org.apache.commons:commons-lang3:3.19.0'
}
```

* Tương tự, nên xử lý vulnerable dependency `logback-core:1.5.18` nếu có

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'ch.qos.logback:logback-core:1.5.19'
}
```

### 3.2 Cấu hình Swagger UI

Swagger UI mặc định tại: `http://localhost:8080/swagger-ui.html`

### 3.3 Tuỳ chỉnh thông tin API

#### 3.3.1 Khai báo trong `application.properties`

```properties
# ===== Swagger / OpenAPI =====
# Info
openapi.title=Student Management API
openapi.description=RESTful API documentation for Student Management application
openapi.version=v1.0.0

# Servers
openapi.servers[0].url=http://localhost:8080${api.prefix}
openapi.servers[0].description=Local Dev

openapi.servers[1].url=https://staging.api.example.com${api.prefix}
openapi.servers[1].description=Staging

openapi.servers[2].url=https://api.example.com${api.prefix}
openapi.servers[2].description=Production

# Groups
openapi.groups.students.name=students
openapi.groups.students.packages=student.management.api_app.controller.student

openapi.groups.demo.name=demo
openapi.groups.demo.packages=student.management.api_app.controller.demo1
```

#### 3.3.2 Tạo file `configs/OpenApiConfig.java` để cấu hình tùy chỉnh

  > * Tự động sinh ra tài liệu API cho ứng dụng, tùy chỉnh thông tin hiển thị (title, version, description, servers...)
  > * Chia thành nhiều nhóm API

```java
//src/main/java/student/management/api_app/configs/OpenApiConfig.java

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenApiConfig {

  // ===== Info =====
  @Value("${openapi.title}")
  String title;
  @Value("${openapi.description}")
  String description;
  @Value("${openapi.version}")
  String version;

  // ===== Servers =====
  @Value("${openapi.servers[0].url}")
  String server0Url;
  @Value("${openapi.servers[0].description}")
  String server0Desc;

  @Value("${openapi.servers[1].url}")
  String server1Url;
  @Value("${openapi.servers[1].description}")
  String server1Desc;

  @Value("${openapi.servers[2].url}")
  String server2Url;
  @Value("${openapi.servers[2].description}")
  String server2Desc;

  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
            .info(new Info()
                    .title(title)
                    .description(description)
                    .version(version))
            .servers(List.of(
                    new Server().url(server0Url).description(server0Desc),
                    new Server().url(server1Url).description(server1Desc),
                    new Server().url(server2Url).description(server2Desc)
            ));
  }

  // ===== Groups =====
  @Value("${openapi.groups.students.name}")
  String studentsGroupName;
  @Value("${openapi.groups.students.packages}")
  String[] studentsPackages;

  @Value("${openapi.groups.demo.name}")
  String demoGroupName;
  @Value("${openapi.groups.demo.packages}")
  String[] demoPackages;

  @Bean
  public GroupedOpenApi studentsGroup() {
    return GroupedOpenApi.builder()
            .group(studentsGroupName)
            .packagesToScan(studentsPackages)
            .build();
  }

  @Bean
  public GroupedOpenApi demoGroup() {
    return GroupedOpenApi.builder()
            .group(demoGroupName)
            .packagesToScan(demoPackages)
            .build();
  }
}
```

> * Annotation `@Bean`
>   * Annotation của Spring dùng để đánh dấu một `method` mà Spring sẽ gọi để tạo ra một `bean` (tức là một đối tượng được quản lý bởi Spring Container)
>   * Đặt trên `method` trong class có `@Configuration`
> * Annotation `@Configuration`
>   * Annotation của Spring dùng để đánh dấu một class cấu hình
>   * Class này chứa các phương thức tạo ra `bean` cho Spring quản lý
>   * `@Configuration` = Nơi định nghĩa các `@Bean` để Spring khởi tạo và đưa vào spring container
>   * SpringDoc (thư viện `springdoc-openapi`) sẽ tự động tìm và sử dụng tất cả các `bean` được định nghĩa trong class OpenApiConfig do Spring đã khởi tạo sẵn  
> * Annotation `@FieldDefaults(level = AccessLevel.PRIVATE)`
>   * Annotation của Lombok, giúp mặc định tất cả các field trong class là private → không cần phải viết `private String title;`
> * Annotation `@Value`
>   * Annotation của Spring, cho phép lấy giá trị từ file `application.properties`
>   * Khi Spring khởi động, nó sẽ inject giá trị tương ứng vào `title`
> * Bean `openApi()`
>   * Method info(...) để set thông tin chung của API (tên, mô tả, phiên bản) cho đối tượng Info()
>   * Method servers(...) để liệt kê list các môi trường deploy
> * Các Bean `studentsGroup()` và `demoGroup()`
>   * Sử dụng `GroupedOpenApi` để tạo nhiều nhóm API riêng biệt trong Swagger UI
>   * `.packagesToScan(...)`: group theo package
>     * Group `students` quét toàn bộ controller trong package `controller.student`
>     * Group `demo` quét package `controller.demo1` và `controller.demo2`

_Lưu ý: Một group cần nhiều package thì sử dụng dấu phẩy_

```properties
openapi.groups.demo.name=demo
openapi.groups.demo.packages=student.management.api_app.controller.demo1,student.management.api_app.controller.demo2
```

> * Ngoài ra có thể sử dụng `.pathsToMatch("/api/v1/students/**")` để group theo path

#### Bài tập 2: Viết class `OpenApiConfig` hoàn chỉnh

```java
//src/main/java/student/management/api_app/configs/OpenApiConfig.java

// Hãy khai báo Annotation cần thiết
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenApiConfig {

  // ===== Info =====
  @Value("${openapi.title}")
  String title;
  @Value("${openapi.description}")
  String description;
  @Value("${openapi.version}")
  String version;

  // ===== Servers =====
  @Value("${openapi.servers[0].url}")
  String server0Url;
  @Value("${openapi.servers[0].description}")
  String server0Desc;

  @Value("${openapi.servers[1].url}")
  String server1Url;
  @Value("${openapi.servers[1].description}")
  String server1Desc;

  @Value("${openapi.servers[2].url}")
  String server2Url;
  @Value("${openapi.servers[2].description}")
  String server2Desc;

  // Hãy khai báo Annotation cần thiết
  public OpenAPI openApi() {
    // Hãy hoàn thiện code
  }

  // ===== Groups =====
  @Value("${openapi.groups.students.name}")
  String studentsGroupName;
  @Value("${openapi.groups.students.packages}")
  String[] studentsPackages;

  @Value("${openapi.groups.demo.name}")
  String demoGroupName;
  @Value("${openapi.groups.demo.packages}")
  String[] demoPackages;

  // Hãy khai báo Annotation cần thiết
  public GroupedOpenApi studentsGroup() {
    // Hãy hoàn thiện code
  }

  // Hãy khai báo Annotation cần thiết
  public GroupedOpenApi demoGroup() {
    // Hãy hoàn thiện code
  }
}
```

#### 3.3.3 Tạo mock StudentController để test Swagger UI

```java
// controller/demo1/DemoController1.java
@RestController
@RequestMapping("${api.prefix}/demo1")
public class DemoController1 {
  @GetMapping
  public ResponseEntity<String> demo1() {
    return ResponseEntity.ok("Hello Demo1");
  }

  @GetMapping("/{id}")
  public ResponseEntity<String> demo1ById(@PathVariable int id) {
    return ResponseEntity.ok("Hello Demo1 By ID");
  }
}

// controller/demo2/DemoController2a.java
@RestController
@RequestMapping("${api.prefix}/demo2a")
public class DemoController2a {
  @GetMapping
  public ResponseEntity<String> demo2a() {
    return ResponseEntity.ok("Hello Demo2a");
  }
}

// controller/demo2/DemoController2b.java
@RestController
@RequestMapping("${api.prefix}/demo2b")
public class DemoController2b {
  @GetMapping
  public ResponseEntity<?> demo2b() {
    return ResponseEntity.ok("Hello Demo2b");
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> demo2bById(@PathVariable int id) {
    return ResponseEntity.ok("Hello Demo2b By ID");
  }
}
```

---

## 4) Thực hành: Thiết kế API `/api/v1/students` trả danh sách rỗng

### 4.1 Controller

```java
//src/main/java/student/management/api_app/controller/student/StudentController.java

@RestController
@RequestMapping("${api.prefix}/students")
@Tag(name = "Student Management", description = "Student Management API")
public class StudentController {

  @Operation(summary = "Get empty student list",
          description = "Bài thực hành buổi 2: Thiết kế API `/api/v1/students` trả danh sách rỗng")
  @GetMapping
  public ResponseEntity<ApiResponse<List<Object>>> getStudents() {
    ApiResponse<List<Object>> studentList = ApiResponse.<List<Object>>builder()
            .success(true)
            .data(List.of())
            .error(null)
            .build(); // khởi tạo đối tượng sau khi đã set các giá trị
    return ResponseEntity.ok(studentList);
  }
}
```

> * Annotation `@RestController`
>   * Controller trong Spring Boot dành cho API (REST API)
>   * Kết hợp giữa:
>     * `@Controller`: xác định class là một controller
>     * `@ResponseBody`: mọi kết quả trả về sẽ tự động được chuyển thành JSON
> * Annotation `@RequestMapping("${api.prefix}/students")`
>   * Xác định đường dẫn gốc (base URL) của tất cả API trong controller này
>   * API này có URL đầy đủ là /api/v1/students
>   * Khi muốn đổi version (v2, v3, …), chỉ cần đổi cấu hình trong file .properties, không cần sửa code
> * Annotation `@Tag(name = "Student Management", description = "Student Management API")`
>   * Annotation của Swagger (OpenAPI), giúp hiển thị nhóm API trên Swagger UI
>   * `name`: tiêu đề nhóm
>   * `description`: mô tả ngắn gọn về nhóm API này
> * Annotation `@Operation`
>   * Annotation của Swagger (OpenAPI), dùng để mô tả chi tiết cho từng API cụ thể
>   * `summary`: mô tả ngắn (hiển thị ngay bên cạnh endpoint)
>   * `description`: mô tả chi tiết hơn khi mở rộng trong Swagger UI
> * Annotation `@GetMapping`
>   * Chỉ định phương thức HTTP là GET
>   * Vì Controller có @RequestMapping("${api.prefix}/students"), nên endpoint này sẽ là `GET /api/v1/students`
> * `ResponseEntity` là wrapper chuẩn của Spring cho HTTP response
>   * `ok(body)` = status code 200
>   * `body`: đối tượng response, ở đây là `studentList`

#### Bài tập 3: Viết class `StudentController` hoàn chỉnh

```java
//src/main/java/student/management/api_app/controller/student/StudentController.java

// Hãy khai báo Annotation cần thiết
public class StudentController {

  // Hãy khai báo Annotation cần thiết
  public ResponseEntity<ApiResponse<List<Object>>> getStudents() {
    // Hãy hoàn thiện code
  }
}
```

### 4.2 Chạy thử ứng dụng

* Mở trình duyệt: `http://localhost:8080/swagger-ui.html`
* Kiểm tra API `/api/v1/students` → Trả danh sách rỗng `[ ]`

---

## 5) Kiểm thử API với Postman

### 5.1 Gửi yêu cầu GET

* **URL:** `http://localhost:8080/api/v1/students`
* **Method:** GET
* **Kết quả mong đợi:**

```json
{
  "success": true,
  "data": [],
  "error": null,
  "timestamp": "..."
}
```

### 5.2 Xuất file collection

> * Tạo collection “Student API”.
> * Lưu request GET `/api/v1/students`.
> * Export JSON file để chia sẻ nhóm

---

## 6) Tài liệu tham khảo

* [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
* [SpringDoc OpenAPI](https://springdoc.org/)
* [Postman Learning Center](https://learning.postman.com/)