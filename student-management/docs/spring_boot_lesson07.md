# Spring Boot – Buổi 7: Spring Data JPA nâng cao

## 1) Ôn lại Query Methods

> Chỉ cần đặt tên method đúng chuẩn, Spring Boot tự sinh truy vấn SQL tương ứng

```java
List<Person> findByFullNameContainingIgnoreCase(String keyword);
Optional<Person> findByEmail(String email);
boolean existsByPhone(String phone);
List<Student> findByPerson_FullNameContainingIgnoreCase(String keyword);
```

## 2) Custom Query Methods (@Query Annotation)

> Khi Query Methods không đủ để diễn tả truy vấn → sử dụng `@Query`
> * Truy vấn phức tạp
> * Nhiều điều kiện linh hoạt
> * Joins nhiều bảng
> * Tối ưu hiệu năng, viết truy vấn cụ thể

`@Query` gồm 2 loại:
* JPQL (Java Persistence Query Language)
* Native SQL Query

### 2.1 Sử dụng JPQL

> JPQL là ngôn ngữ truy vấn dựa trên Entity trong Java, KHÔNG dựa trên bảng trong DB
> * JPQL truy vấn Entity và mối quan hệ giữa các Entity → trả về Java object
> * Hibernate sẽ dịch JPQL sang SQL thực tế phù hợp với DB bên dưới

```java
@Query("SELECT s FROM Student s WHERE s.email = :email")
Optional<Student> findByEmailCustom(@Param("email") String email);
```

| Thành phần                     | Ý nghĩa                                      |
|--------------------------------|----------------------------------------------|
| `@Query(...)`                  | Khai báo JPQL                                |
| `Student s`                    | `Student` là Entity, `s` là alias            |
| `s.email`                      | Field email trong class Student              |
| `:email`                       | `Named parameter` sẽ được gán từ @Param      |
| `Optional<Student>`            | Trả về một đối tượng Student nếu có          |
| `@Param("email") String email` | Khai báo tham số "email" để gán vào `:email` |


> **Lưu ý**: JPQL không dùng `*`

#### Nghiệp vụ thực tế:

##### 1. Tìm Student theo số điện thoại (JOIN Entity)

```java
@Query("""
    SELECT s FROM Student s
    JOIN s.person p
    WHERE p.phone = :phone
""")
Optional<Student> findByPhone(@Param("phone") String phone);
```

`JOIN s.person p`: Lấy Student và JOIN sang Person thông qua trường `person` mà Student đã mapping
* `s.person` chính là truy cập đến Entity liên kết
* Alias `p`: đại diện cho `Person`
* Điều kiện join là `person_id`

```java
@MapsId
@OneToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "person_id")
Person person;
```

##### 2. Thống kê số student theo năm nhập học (GROUP BY)

* DTO `EnrollmentStatDTO`:

```java
public record EnrollmentStatDTO(Integer year, Long total) {}
```

* Trường hợp JPQL trả về DTO:
  * Cần khởi tạo đối tượng EnrollmentStatDTO ngay trong SELECT
  * Cần khai báo đầy đủ đường dẫn source root của EnrollmentStatDTO

```java
@Query("""
    SELECT new student.management.api_app.dto.student.EnrollmentStatDTO(
        s.enrollmentYear,
        COUNT(s)
    )
    FROM Student s
    GROUP BY s.enrollmentYear
""")
List<EnrollmentStatDTO> countStudentsGroupedByYear();
```

* Trường hợp JPQL trả về `List<Object[]>`:

```java
@Query("""
    SELECT s.enrollmentYear, COUNT(s)
    FROM Student s
    GROUP BY s.enrollmentYear
""")
List<Object[]> countStudentsGroupedByYear();
```

**Lưu ý kiểu trả về**:

| Truy vấn trả về | JPQL dùng                 | Kiểu trả về               |
|-----------------|---------------------------|---------------------------|
| Entity          | `SELECT s`                | `List<Student>`           |
| 1 cột đơn giản  | `SELECT s.enrollmentYear` | `List<Integer>`           |
| Nhiều cột       | `SELECT s.year, COUNT(s)` | `List<Object[]>` hoặc DTO |

* Trong dự án thực tế → luôn dùng DTO
* `Object[]` chỉ nên dùng để test nhanh hoặc explore DB

#### Bài tập 1: Viết 2 API cho 2 phương thức `Optional<Student> findByPhone()` và `List<EnrollmentStatDTO> countStudentsGroupedByYear` vừa bổ sung trong `StudentRepository`

* `@GetMapping("/by-phone")`: API tìm học viên theo điện thoại 
* `@GetMapping("stats/count-by-year")`: API thống kê số lượng student theo enrollmentYear

### 2.2 Native SQL Query

Ngoài JPQL, Spring Data JPA cho phép viết Native SQL trực tiếp trong `@Query`

> Native SQL = viết đúng cú pháp SQL của database (PostgreSQL / MySQL / ...) <br/>
> Không dựa trên Entity như JPQL, mà dùng tên bảng + tên cột thật trong DB

Dùng khi:
* Cần dùng tính năng đặc thù DB: `LIMIT`, `OFFSET`, `ILIKE`, `ARRAY`, `JSONB`, `WITH`, `WINDOW FUNCTION`, ...
* Câu query rất phức tạp, JOIN nhiều bảng, subquery, tối ưu hiệu năng ở mức SQL
* Muốn tối ưu truy vấn mà JPQL diễn tả khó hoặc dài dòng

```java
@Query(
      value = "SELECT * FROM app.students WHERE student_code = :code",
      nativeQuery = true
)
Optional<Student> findByStudentCodeNative(@Param("code") String code);
```

| Thành phần                   | Ý nghĩa                                       |
|------------------------------|-----------------------------------------------|
| `value = "..."`              | Viết SQL thuần (native SQL)                   |
| `nativeQuery = true`         | Báo cho Spring đây là native, không phải JPQL |
| `SELECT * FROM app.students` | Dùng tên schema + tên bảng thật trong DB      |
| `student_code`               | Dùng tên cột trong DB                         |
| `Optional<Student>`          | Spring map kết quả về Entity `Student`        |

> Lưu ý: Native query phụ thuộc vào tên bảng/cột & cú pháp của DB thực tế

#### Ví dụ: sử dụng Native query để triển khai lại nghiệp vụ "Thống kê số student theo năm nhập học (GROUP BY)"

```java
public interface EnrollmentStatView {
    Integer getYear();
    Long getTotal();
}
```

```java
@Query(value = """
    SELECT
        s.enrollment_year AS year,
        COUNT(*) AS total
    FROM app.students s
    GROUP BY s.enrollment_year
""", nativeQuery = true)
List<EnrollmentStatView> countStudentsGroupedByYearNative();
```

#### Các điểm quan trọng cần nhớ đối với Native query:

* `EnrollmentStatView` phải là `interface`, chứ không phải là `class`/`record` như JPQL
  * SQL thuần không thể nhận biết class nào trong Java cả → không thể viết `SELECT new ...DTO(...)`
  * Thay vào đó Spring Data JPA hỗ trợ **interface-based projection**:
    * Spring tự map theo alias → getter
    * Bắt buộc phải có alias cột đúng tên getter (`AS year`, `AS total`) để Map vào phương thức getter tương ứng (`getYear()`, `getTotal()`)
* Native query phụ thuộc DB cụ thể (PostgreSQL / MySQL / ...) → đổi DB thì phải sửa code rất nhiều

> Hãy dùng JPQL nếu đủ xài <br>
> Chỉ dùng Native SQL khi thật sự biết mình đang làm gì và cần sức mạnh của DB thật

---

## 3) JPA Specification – Dynamic Query

JPA Specification là cách viết truy vấn động (Dynamic Query) trong Spring Data JP để:
* Tạo query linh hoạt (filter nhiều trường hợp tuỳ chọn)
* Dễ tái sử dụng điều kiện ở nhiều nơi
* Không cần viết chuỗi `if else` nối query thủ công
* Giữ code gọn gàng, type-safe

Ví dụ: tìm student theo tên, email, tuổi tối thiểu… nhưng tất cả đều optional

### 3.1 Repository hỗ trợ Specification

Để dùng Specification, Repository phải kế thừa thêm `JpaSpecificationExecutor`:

```java
public interface StudentRepository
        extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {
}
```

`JpaSpecificationExecutor` giúp repository có thêm các hàm:
* `findAll(Specification spec)`
* `findAll(Specification spec, Pageable pageable)`
* `count(Specification spec)`
* `exists(Specification spec)`

### 3.2 Specification là gì

Trong Spring Data JPA:

```java
public interface Specification<T> {
    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
```

* `Specification<T>` là một Functional interface của Spring Data JPA, đại diện cho một điều kiện trong mệnh đề WHERE của câu SQL
* Functional Interface = interface chỉ có 1 method abstract
* Vì chỉ có 1 method → dùng biểu thức lambda `(root, query, cb) -> Predicate` để implement method `toPredicate()`

| Tham số                  | Ý nghĩa                                                                              |
|--------------------------|--------------------------------------------------------------------------------------|
| `root`                   | đại diện cho **Entity gốc** (ví dụ `Student`)                                        |
| `query`                  | đại diện cho câu truy vấn hiện tại (để thêm sort, distinct... nếu muốn)              |
| `cb` (`CriteriaBuilder`) | là công cụ để tạo các biểu thức điều kiện, `cb` dùng `Expression` để tạo `Predicate` |

* `Expression` trong JPA Criteria API: 
  * `Expression` = Biểu diễn một cột hoặc biểu thức trong SQL
  * Nó là một đối tượng biểu diễn một giá trị, một cột, hay một phép toán dùng trong WHERE, SELECT, ORDER BY
  * `cb` dùng các `Expression` sau để tạo câu SQL `WHERE LOWER(full_name) LIKE '%an%'`:

    | SQL                          | Loại      | Expression (Criteria API)                         |
    |------------------------------|-----------|---------------------------------------------------|
    | full_name                    | cột       | `root.get("fullName")`                            |
    | LOWER(full_name)             | method    | `cb.lower(root.get("fullName"))`                  |
    | '%an%'                       | chuỗi     | chuỗi thường                                      |
    | LOWER(full_name) LIKE "%an%" | biểu thức | `cb.like(cb.lower(root.get("fullName")), "%an%")` |

### 3.3 Tạo các Specification con

Có thể định nghĩa từng điều kiện filter riêng trong class tiện ích với các static method:

```java
public class StudentSpecifications {

    // Tìm theo tên (chứa keyword)
    public static Specification<Student> nameContains(String keyword) {
        return (root, query, cb) -> cb.like(
              cb.lower(root.get("person").get("fullName")), "%" + keyword.toLowerCase() + "%");
    }
  
    // Tìm theo năm nhập học >= year
    public static Specification<Student> enrollmentYearGte(Integer year) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(
              root.get("enrollmentYear"), year);
    }
}
```

#### Giải thích code thực tế của JPA implement Specification.toPredicate()

Khi sử dụng biểu thức lambda để implement Specification.toPredicate()

```java
public static Specification<Student> nameContains(String keyword) {
        return (root, query, cb) -> { cb.like("...", "..."); };
}
```

sẽ tương đương với:

```java
public static Specification<Student> nameContains(String keyword) {
    return new Specification<Person>() {
        @Override
        public Predicate toPredicate(
                Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    
            if (!StringUtils.hasText(keyword)) return null;
      
            return cb.like(
                  cb.lower(root.get("fullName")),
                  SpecUtils.likePattern(keyword));
        }
    };
}
```

* Khi được gọi sẽ khởi tạo một đối tượng `Specification` có method `toPredicate()` đã được implement
* Ở `Service` gọi `StudentSpecifications.nameContains()` để khởi tạo đối tượng `Specification`
  * `Specification<Person> spec = StudentSpecifications.nameContains("an");`
* `spec` này sẽ được truyền vào các method của JPA repository để sử dụng
  * `List<Person> result = personRepo.findAll(spec);`
* Spring Data sẽ gọi `spec.toPredicate(root, query, cb)` để tạo `Predicate` và chuyển nó cho Hibernate sinh SQL

### 3.4 Áp dụng nhiều điều kiện động

Khi muốn tìm học viên theo nhiều filter tuỳ chọn:
* `name`: tìm theo tên (optional)
* `minYear`: năm nhập học tối thiểu (optional)

```java
// Service

public List<Student> search(String name, Integer minYear, Integer maxYear) {
    Specification<Student> spec = Specification.where(null);
  
    if (name != null && !name.isBlank()) {
      spec = spec.and(StudentSpecifications.nameContains(name));
    }
  
    if (minYear != null) {
      spec = spec.and(StudentSpecifications.enrollmentYearGte(minYear));
    }
  
    return studentRepository.findAll(spec);
}
```

Sau khi gắn các điều kiện tìm kiếm (`StudentSpecifications.nameContains(name)`, ...) vào `Specification<Student> spec`:
* Sử dụng `studentRepository.findAll(spec)` để thực thi query động với `spec` đó 
* Truy vấn SQL tự động được hibernate build theo dữ liệu đầu vào
  * Nếu người dùng chỉ nhập `name` → chỉ thêm điều kiện `LIKE`
  * Nếu người dùng nhập cả `minYear` và `maxYear` → thêm 2 điều kiện `BETWEEN`

### 3.5 SQL Hibernate tự sinh

Khi người dùng truyền: `name = "An", minYear = 2022` thì Hibernate log sẽ in ra:

```sql
select
    s1_0.person_id,
    s1_0.enrollment_year,
    s1_0.student_code,
    s1_0.created_at,
    s1_0.updated_at
from app.students s1_0
join app.people p1_0 on p1_0.id = s1_0.person_id
where lower(p1_0.full_name) like '%an%'
  and s1_0.enrollment_year >= 2022
```

→ Mỗi điều kiện trong Specification tương ứng một phần của WHERE

### 3.6 Kết hợp OR, AND, NOT

Có thể linh hoạt kết hợp điều kiện bằng and(), or(), not():

```java
Specification<Student> spec = Specification
        .where(StudentSpecifications.nameContains("An"))
        .or(StudentSpecifications.nameContains("Bình"))
        .and(StudentSpecifications.enrollmentYearGte(2020));
```

Sinh ra SQL tương đương:

```sql
WHERE (lower(full_name) LIKE '%an%' OR lower(full_name) LIKE '%bình%')
  AND enrollment_year >= 2020
```

---

## 4) Pagination & Sorting

> Vấn đề: Nếu bảng `students` có hàng ngàn bản ghi, việc trả toàn bộ danh sách một lúc sẽ:
> * Gây tốn RAM trên server
> * Làm chậm API
> * Frontend phải tải dữ liệu quá lớn, trải nghiệm người dùng kém
> 
> ⇒ Giải pháp: phân trang (pagination) và sắp xếp (sorting)

### 4.1 Pageable trong Spring Data JPA

Spring Data JPA cung cấp interface `Pageable` để biểu diễn yêu cầu phân trang.
Có thể tạo đối tượng `Pageable` bằng `PageRequest.of()`

```java
Pageable pageable = PageRequest.of(0, 10, Sort.by("enrollmentYear").descending());
Page<Student> page = studentRepository.findAll(pageable);
```

* `0`: Số trang (page index, bắt đầu từ 0)
* `10`: Kích thước trang (mỗi trang 10 bản ghi)
* `Sort.by("enrollmentYear").descending()`: Sắp xếp theo năm nhập học giảm dần

### 4.1.1 Request từ client

Spring Boot tự động map query params vào `Pageable` nếu nó được thêm vào controller:

```java
@GetMapping
public ResponseEntity<Page<Student>> getAll(@ParameterObject Pageable pageable) {
    Page<Student> page = studentRepository.findAll(pageable);
    return ResponseEntity.ok(page);
}
```

Chỉ cần có param `Pageable` trong method `getAll(@ParameterObject Pageable pageable)` của controller (`@GetMapping`), khi client gọi 

```bash
  GET /api/v1/students?page=0&size=2&sort=studentCode,desc&sort=enrollmentYear,desc
```

→ Spring sẽ tự tạo đối tượng `Pageable` tương ứng với query params từ request. Khi gọi `studentRepository.findAll(pageable)`, hibernate sẽ tự động sinh các SQL:

**SQL #1**: Load danh sách sinh viên từ bảng app.students để lấy dữ liệu Student đúng trang, đúng thứ tự sort

```sql
Hibernate: 
    select
        s1_0.person_id,
        s1_0.created_at,
        s1_0.enrollment_year,
        s1_0.student_code,
        s1_0.updated_at 
    from
        app.students s1_0 
    order by
        s1_0.student_code desc,
        s1_0.enrollment_year desc 
    offset
        ? rows 
    fetch
        first ? rows only
```

`offset 0 rows fetch first 2 rows only`: theo chuẩn ANSI SQL
* Tương đương `OFFSET 0 LIMIT 2` trong Postgres
* Bỏ qua 0 dòng đầu tiên, sau đó chỉ lấy 2 dòng kế tiếp

**SQL #2** Đếm tổng số bản ghi để tính `totalElements` và `totalPages` trong `Page<Student>`

```sql
Hibernate: 
    select
        count(s1_0.person_id) 
    from
        app.students s1_0
```

**SQL #3 & 4**: Lazy loading quan hệ `Person` trong `Student` để load dữ liệu `Person` tương ứng từng `Student` khi mapper truy cập field

```sql
Hibernate: 
    select
        p1_0.id,
        p1_0.address,
        p1_0.contact_email,
        p1_0.created_at,
        p1_0.dob,
        p1_0.full_name,
        p1_0.phone,
        p1_0.updated_at 
    from
        app.people p1_0 
    where
        p1_0.id=?
```

Trong page có 2 Student → 2 query `select ... from app.people where id=?` bị lặp lại (gọi là N+1 problem)
* Hiện tại: 1 query Student + N query Person (N = số sinh viên trong trang)
* Cần tối ưu N+1 problem → sẽ học ở bài sau

### 4.2 Cấu trúc dữ liệu trả về (Page object)

Đối tượng `Page<Student>` chứa nhiều thông tin của `Pageable`:

| Phương thức          | Ý nghĩa                              |
|----------------------|--------------------------------------|
| `getContent()`       | Danh sách phần tử của trang hiện tại |
| `getNumber()`        | Số trang hiện tại (bắt đầu từ 0)     |
| `getSize()`          | Số bản ghi mỗi trang                 |
| `getTotalElements()` | Tổng số bản ghi                      |
| `getTotalPages()`    | Tổng số trang                        |
| `hasNext()`          | Có trang kế tiếp không               |
| `hasPrevious()`      | Có trang trước đó không              |

#### Ví dụ trả về mặc định của `Page<Student>`:

```json
{
  "content": [
    { "studentCode": "ST001", "enrollmentYear": 2023 },
    { "studentCode": "ST002", "enrollmentYear": 2023 }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 2 },
  "totalElements": 120,
  "totalPages": 60,
  "last": false,
  "first": true
}
```

#### Ý nghĩa các field trong JSON trả về:

Không trả toàn bộ danh sách 120 sinh viên, mà chỉ trả từng trang nhỏ (mỗi trang 2 sinh viên)
* `"content"`: danh sách sinh viên thuộc trang hiện tại (page 0)
  * Mỗi phần tử là một `Student` (ở dạng JSON)
  * Tương đương với `page.getContent()`
* `"pageable"`: thông tin về trang hiện tại
  * `pageNumber`: chỉ số trang hiện tại (bắt đầu từ 0 trong Spring)
  * `pageSize`: số phần tử trong mỗi trang (ở đây là 2 sinh viên/trang)
* `"totalElements"`: có tổng cộng 120 sinh viên trong DB
  * Tổng số phần tử (bản ghi) trong toàn bộ dataset
  * Không chỉ riêng trang hiện tại
* `"totalPages"`: tổng số trang
* `"last"`: cho biết trang hiện tại có phải trang cuối cùng không
* `"first"`: cho biết trang hiện tại có phải trang đầu tiên không

### 4.3 Tạo Custom Response cho Frontend

Frontend thường không cần toàn bộ metadata phức tạp của `Page<Student>`, mà chỉ cần:

```json
{
  "items": [
    { "field1": "...", "field2": "..." }, 
    { "field1": "...", "field2": "..." }
  ],
  "page": 0,
  "size": 5,
  "totalItems": 120,
  "totalPages": 24
}
```

#### Best practice cho phân trang:

* Nên chuyển đổi từ `Page<Student>` sang PageResponse DTO để FE dễ đọc:

```java
// student/management/api_app/dto/page/PageResponse.java

import org.springframework.data.domain.Page; // Sử dụng Page của Spring

public class PageResponse<T> {
    private List<T> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;

    public PageResponse(Page<T> pageData) {
        this.items = pageData.getContent();
        this.page = pageData.getNumber();
        this.size = pageData.getSize();
        this.totalItems = pageData.getTotalElements();
        this.totalPages = pageData.getTotalPages();
    }
}
```

* Khi đó ở `service`, phương thức `getAll()` trả về `PageResponse<StudentListItemResponse>` thay vì `List<StudentListItemResponse>`:

```java
public PageResponse<StudentListItemResponse> getAll(Pageable pageable) {
    // Dùng findAll(Pageable pageable) của PagingAndSortingRepository
    Page<Student> pageData = studentRepo.findAll(pageable);
    
    Page<StudentListItemResponse> mappedPageData =
            pageData.map(studentMapper::toListItemResponse);
    return new PageResponse<>(mappedPageData);
}
```

* Ở controller chỉ cần:

```java
@GetMapping
public ResponseEntity<AppResponse<PageResponse<StudentListItemResponse>>> getAll(
        // Khai báo spring-doc cho pageable là param object để Swagger UI hiển thị đúng
        @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(AppResponse.<PageResponse<StudentListItemResponse>>builder()
            .success(true)
            .data(service.getAll(pageable))
            .build());
}
```

**Lưu ý**: controller cần xóa `@GetMapping getAll()` cũ (trả về list), vì controller không thể có cùng lúc 2 URL `GET /api/v1/students`
→ gây lỗi "Ambiguous mapping" ở runtime

* Tạo Factory Method `AppResponse.success()` và `AppResponse.error()` → tránh lặp code `AppResponse.builder().build()`:

```java
// student/management/api_app/dto/AppResponse.java

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppResponse<T> {
    boolean success;
    T data; // List/Object/Null
    AppError error;

    @Builder.Default
    Instant timestamp = Instant.now();

    // No need for ApiError if using the RFC 7807 standard (Problem Details)
    @Value
    @Builder
    public static class AppError {
        String code;
        String message;
        String path;
    }

    // Factory method (static helper methods)
    public static <T> AppResponse<T> success(T data) {
        return AppResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> AppResponse<T> error(String code, String message, String path) {
        return AppResponse.<T>builder()
                .success(false)
                .error(AppError.builder()
                        .code(code)
                        .message(message)
                        .path(path)
                        .build())
                .build();
    }
}
```

Khi đó Controller chỉ cần gọi `AppResponse.success()`:

```java
@GetMapping
public ResponseEntity<AppResponse<PageResponse<StudentListItemResponse>>> getAll(
        @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(AppResponse.success(service.getAll(pageable)));
}
```

### 4.4 Kết hợp Pagination với Specification hoặc @Query

* `JpaSpecificationExecutor` có hỗ trợ `Pageable` như method `Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable)`:

```java
// Service

Specification<Student> spec = Specification
        .where(StudentSpecifications.nameContains(keyword))
        .and(StudentSpecifications.enrollmentYearGte(minYear));

Page<Student> page = studentRepository.findAll(spec, pageable);
```

* Có thể kết hợp `Pageable` với JPQL ở tầng repository:

```java
// Repository

@Query("""
    SELECT s FROM Student s
    JOIN s.person p
    WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
Page<Student> search(@Param("keyword") String keyword, Pageable pageable);

```

### 4.5 Sorting - Sắp xếp dữ liệu linh hoạt

> Cho phép client chọn thứ tự hiển thị dữ liệu:
> * Tăng dần theo tên
> * Giảm dần theo năm nhập học
> * Hoặc kết hợp nhiều tiêu chí sắp xếp

Spring Data JPA hỗ trợ `Sort` như một đối tượng độc lập hoặc kết hợp với `Pageable`

#### 4.5.1 Sort cơ bản trong Spring Data JPA

Sort là class đại diện cho thứ tự sắp xếp trong truy vấn:

```java
Sort sort = Sort.by("enrollmentYear").ascending();
List<Student> students = studentRepository.findAll(sort);
```

Hibernate sẽ tự động sinh SQL tương ứng:

```sql
SELECT * FROM app.students ORDER BY enrollment_year ASC;
```

#### 4.5.2 Sort kết hợp trong Pageable

`Pageable` có thể chứa `Sort` bên trong để phân trang + sắp xếp cùng lúc:

```java
Pageable pageable = PageRequest.of(0, 10, Sort.by("enrollmentYear").descending());
Page<Student> page = studentRepository.findAll(pageable);
```

#### 4.5.3 Truyền Sort từ request client

Spring Boot hỗ trợ tự động parse `sort` từ query string:

```bash
  GET /api/v1/students?page=0&size=5&sort=enrollmentYear,desc&sort=studentCode,asc
```

Request này sẽ được Spring tự động tạo `Pageable` có `Sort` tương ứng ở bên trong

#### 4.5.4 Sort theo thuộc tính trong entity liên kết (Join Sort)

Trường hợp muốn sắp xếp Student theo `fullName` của `Person`:
* `Student` có quan hệ OneToOne với `Person`, dùng `Sort.Order` có cú pháp nested property:

```java
Sort sort = Sort.by(Sort.Order.asc("person.fullName"));
List<Student> students = studentRepository.findAll(sort);
```

Hibernate sẽ tự động join và sinh SQL tương ứng:

```sql
SELECT s.*
FROM app.students s
JOIN app.people p ON p.id = s.person_id
ORDER BY p.full_name ASC;
```

---

## 5) Thực hành: API Tìm kiếm và sắp xếp cho Student

### 5.1 DTO: StudentSearchRequest

```java
// student/management/api_app/dto/student/StudentSearchRequest.java

public record StudentSearchRequest(
        String name,
        String phone,
        String email,
        
        String studentCode,
        Integer enrollmentYearFrom,
        Integer enrollmentYearTo
) {}
```

> Dùng @ParameterObject để Swagger + Spring Doc hiểu khi tự động bind từ query param

Ví dụ request:

```bash
  GET /api/v1/students/search?
      name=an&
      enrollmentYearFrom=2022&
      enrollmentYearTo=2024&
      page=0&size=10&
      sort=person.fullName,asc&
      sort=enrollmentYear,desc
```

### 5.2 Tạo các Specification con

Cần chú ý các điểm sau:
* Nếu filter null / rỗng → trả null → Spring Data JPA sẽ bỏ qua điều kiện filter đó
* Dùng `root.join("person")` để join theo mapping OneToOne

```java
// student/management/api_app/repository/specification/StudentSpecifications.java

public class StudentSpecifications {

    private static String likePattern(String input) {
        return "%" + input.toLowerCase().trim() + "%";
    }
  
    public static Specification<Student> personNameContains(String keyword) {
        return (root, query, cb) -> {
          if (!StringUtils.hasText(keyword)) return null;
          Join<Student, Person> person = root.join("person", JoinType.INNER);
          return cb.like(cb.lower(person.get("fullName")), likePattern(keyword));
        };
    }
  
    public static Specification<Student> personPhoneEquals(String phone) {
        return (root, query, cb) -> {
          if (!StringUtils.hasText(phone)) return null;
          Join<Student, Person> person = root.join("person", JoinType.INNER);
          return cb.equal(person.get("phone"), phone);
        };
    }
  
    public static Specification<Student> personEmailContains(String email) {
        return (root, query, cb) -> {
          if (!StringUtils.hasText(email)) return null;
          Join<Student, Person> person = root.join("person", JoinType.INNER);
          return cb.like(cb.lower(person.get("contactEmail")), likePattern(email));
        };
    }
  
    public static Specification<Student> studentCodeContains(String code) {
        return (root, query, cb) -> {
          if (!StringUtils.hasText(code)) return null;
          return cb.like(cb.lower(root.get("studentCode")), likePattern(code));
        };
    }
  
    public static Specification<Student> enrollmentYearGte(Integer from) {
        return (root, query, cb) -> {
          if (from == null) return null;
          return cb.greaterThanOrEqualTo(root.get("enrollmentYear"), from);
        };
    }
  
    public static Specification<Student> enrollmentYearLte(Integer to) {
        return (root, query, cb) -> {
          if (to == null) return null;
          return cb.lessThanOrEqualTo(root.get("enrollmentYear"), to);
        };
    }
}
```

`Join<Student, Person> person = root.join("person", JoinType.INNER)`:
* `root`: entity gốc ở đây là `Student`
* `.join("person", ...)`: tạo lệnh JOIN sang thuộc tính `person` trong entity `Student`
* `"person"`: tên field trong entity `Student` có annotation `@OneToOne`
* `JoinType.INNER`: kiểu INNER JOIN trong SQL
* `Join<Student, Person>`: Kiểu dữ liệu trả về — biểu diễn quan hệ giữa `Student` và `Person`

### 5.3 Service

* `IStudentService`: Nâng cấp `getAll()` trả phân trang, thay thế `searchByPersonName()` bằng `search()`

```java
// student/management/api_app/service/IStudentService.java

PageResponse<StudentListItemResponse> getAll(Pageable pageable);
PageResponse<StudentListItemResponse> search(StudentSearchRequest req, Pageable pageable);
```

* `StudentService`

```java
// student/management/api_app/service/impl/StudentService.java

@Transactional(readOnly = true)
@Override
public PageResponse<StudentListItemResponse> getAll(Pageable pageable) {
    // Dùng findAll(Pageable pageable) của PagingAndSortingRepository
    Page<Student> pageData = studentRepo.findAll(pageable);

    Page<StudentListItemResponse> mappedPageData =
            pageData.map(studentMapper::toListItemResponse);
    return new PageResponse<>(mappedPageData);
}

@Transactional(readOnly = true)
@Override
public PageResponse<StudentListItemResponse> search(
        StudentSearchRequest req, Pageable pageable) {    
        
    // Chuẩn hóa làm sạch input
    String name = trimToNull(req.name());
    String phone = normalizePhone(req.phone());
    String email = normalizeEmail(req.email());
    String code = normalizeCode(req.studentCode());
  
    Specification<Student> spec = Specification.<Student>unrestricted()
            .and(personNameContains(name))
            .and(personPhoneEquals(phone))
            .and(personEmailContains(email))
            .and(studentCodeContains(code))
            .and(enrollmentYearGte(req.enrollmentYearFrom()))
            .and(enrollmentYearLte(req.enrollmentYearTo()));
  
    Page<Student> pageData = studentRepo.findAll(spec, pageable);
  
    Page<StudentListItemResponse> mappedPageData =
            pageData.map(studentMapper::toListItemResponse);
  
    return new PageResponse<>(mappedPageData);
}
```

* Pageable đã chứa thông tin page, size, và sort (bao gồm cả sort theo person.fullName nếu client gửi)
* Ví dụ request từ client:

```bash
  GET /api/v1/students/search?
    name=an&
    enrollmentYearFrom=2022&
    page=0&size=5&
    sort=person.fullName,asc&
    sort=enrollmentYear,desc
```

### 5.4 Controller

* `GET /api/v1/students/search` có đầy đủ pagination + sort
* Điều chỉnh lại method `search()`:

```java
// student/management/api_app/controller/student/StudentController.java

@Operation(
        summary = "Search students by attributes",
        description = """
                Tìm kiếm học viên với nhiều điều kiện tùy chọn:
                - name: chứa trong Person.fullName (ignore case)
                - phone: đúng với Person.phone (sau normalize)
                - email: chứa trong Person.contactEmail
                - studentCode: chứa trong studentCode
                - enrollmentYearFrom / enrollmentYearTo: khoảng năm nhập học
                Hỗ trợ phân trang & sort theo mọi field hợp lệ (kể cả person.fullName).
                """,
        responses = @ApiResponse(responseCode = "200", description = "Success")
)
@GetMapping("/search")
public ResponseEntity<AppResponse<PageResponse<StudentListItemResponse>>> search(
        @ParameterObject StudentSearchRequest req, // Để Swagger + Spring Doc hiểu khi bind từ query param
        @ParameterObject @PageableDefault(
                size = 5, sort = {"createdAt", "person.fullName"}, direction = Sort.Direction.DESC)
        Pageable pageable
) {
  return ResponseEntity.ok(AppResponse.success(service.search(req, pageable)));
}
```

#### Bài tập 2: Hãy nâng cấp trả phân trang cho các API sau:

* `GET /api/v1/students/by-year`
* `GET /api/v1/persons`
* `GET /api/v1/persons/list-by-ids`

#### Bài tập 3: Hãy nâng cấp API `GET /api/v1/persons/search` để:

* Trả phân trang
* Tích hợp specification để search theo `fullName`, `dob`, `phone`, `contactEmail`, `address`

#### Bài tập 4: Hãy thực hiện refactor cho: 

* `StudentSearchRequest`: để lồng `PersonSearchRequest` vào trong `StudentSearchRequest` (cần lưu ý xử lý trường hợp client truyền `null` tất cả các field `PersonSearchRequest` để tránh lỗi `NullPointerException`)
* `StudentSpecifications`: để tái sử dụng các method của `PersonSpecifications`

**Gợi ý**: 

Trong `PersonSpecifications` nên viết thành từng cặp:

```java
// PersonSpecifications

public static Specification<Person> fullNameContains(String keyword) {
    return (root, query, cb) -> {
        if (!StringUtils.hasText(keyword)) return null;
        return cb.like(cb.lower(root.get("fullName")), SpecUtils.likePattern(keyword));
    };
}

public static Predicate fullNameContains(
        Join<?, Person> personJoin, CriteriaBuilder cb, String keyword) {
    if (!StringUtils.hasText(keyword)) return null;
    return cb.like(cb.lower(personJoin.get("fullName")), SpecUtils.likePattern(keyword));
}
```

Như vậy `fullNameContains()` 1 tham số được dùng ở PersonService để tạo Specification<Student> như bình thường,
còn `fullNameContains()` 3 tham số sẽ được tái sử dụng bên `StudentSpecifications`:

```java
// StudentSpecifications

public static Specification<Student> personNameContains(String keyword) {
    return (root, query, cb) -> {
        Join<Student, Person> personJoin = root.join("person", JoinType.INNER);
        return PersonSpecifications.fullNameContains(personJoin, cb, keyword);
    };
}
```
