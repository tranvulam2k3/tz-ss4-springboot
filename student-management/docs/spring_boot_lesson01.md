# Spring Boot – Buổi 1: Tổng quan Web & Project Setup

## 1) Kiến trúc Web: Client–Server, HTTP lifecycle, REST stateless

### 1.1 Client–Server

> * **Client** (Browser, Mobile app, Postman) gửi **HTTP request** → **Server** xử lý → trả **HTTP response**.
> * Tách biệt concerns: UI/UX ở client, business logic + dữ liệu ở server → dễ mở rộng, thay thế từng phần.

### 1.2 HTTP Lifecycle (Request → Response)

> 1. Client tạo request (method, URL, headers, body).
> 2. Request qua Internet đến server.
> 3. Server (Spring Boot) xử lý: lọc (filter) → controller → service → repository → database.
> 4. Server trả response (status code, headers, body JSON) → client hiển thị/tiếp tục gọi API khác.

### 1.3 REST & Stateless

> * **REST** là style thiết kế API dựa trên HTTP, tài nguyên (resources) được mô hình hóa bằng URL.
> * **Stateless**: Mỗi request **tự chứa đủ ngữ cảnh** để server xử lý; server không lưu session state giữa các request (hoặc giảm tối đa).
> * **Idempotent**: `GET`, `PUT`, `DELETE` nên idempotent (gọi lặp lại không thay đổi kết quả), `POST` thường không idempotent.

---

## 2) HTTP Protocol: methods, status codes, headers, body

### 2.1 Methods

| Method   | Dùng cho                                         | Đặc tính                 |
|----------|--------------------------------------------------|--------------------------|
| `GET`    | Lấy tài nguyên                                   | **Safe**, **Idempotent** |
| `POST`   | Tạo mới / hành động (tính toán, hủy đơn hàng...) | **Not idempotent**       |
| `PUT`    | Cập nhật thay thế toàn bộ                        | **Idempotent**           |
| `PATCH`  | Cập nhật một phần                                | Không đảm bảo idempotent |
| `DELETE` | Xóa tài nguyên                                   | **Idempotent**           |

### 2.2 Status Codes

> * **2xx**: Thành công (200 OK, 201 Created, 204 No Content)
> * **3xx**: Chuyển hướng (301, 302, 304 Not Modified)
> * **4xx**: Lỗi phía client (400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict...)
> * **5xx**: Lỗi server (500 Internal Server Error, 503 Service Unavailable)

### 2.3 Headers & Body

> * **Headers**: `Content-Type: application/json`, `Accept: application/json`, `Authorization: Bearer <token>`
> * **Body**: dữ liệu JSON/XML/Form; với REST hiện đại chủ yếu **JSON**.

### 2.4 Demo với `Postman`

---

## 3) Giới thiệu Spring Boot 3.x

> * **Starter dependencies**: gói sẵn dependencies theo chức năng (`spring-boot-starter-web`, `data-jpa`, `validation`, ...).
> * **Auto-Configuration**: tự cấu hình dựa trên classpath + properties.
> * **Actuator**: endpoint giám sát/health (`/actuator/health`).

### 3.1 Tạo project nhanh

> * **Spring Initializr**: [https://start.spring.io](https://start.spring.io) (chọn Java 21, Spring Boot 3.x)
> * Một vài Dependencies mẫu: `Spring Web`, `Spring Data JPA`, `Validation`, `PostgreSQL Driver`, `Lombok`, `Flyway`,... *(tùy chọn thêm: Actuator, DevTools,...)*

---

## 4) Maven/Gradle: quản lý dependencies

### 4.1 Maven (pom.xml – rút gọn)

```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
    <relativePath/>
  </parent>
  <groupId>com.example</groupId>
  <artifactId>demo</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <properties>
    <java.version>21</java.version>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

### 4.2 Gradle (build.gradle – Groovy DSL)

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.6'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'student.management'
version = '0.0.1-SNAPSHOT'
description = 'Project of API App for Student Management'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories { mavenCentral() }

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-database-postgresql'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'org.postgresql:postgresql'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### 4.3 Setting Lombok trên IntelliJ IDEA

#### 4.3.1 Cài plugin Lombok
> * `Setting` → `Plugins`
> * Vào `Marketplace`, tìm Lombok → cài đặt → restart IntelliJ

#### 4.3.2 Bật `annotation processing` cho Lombok
> * `Setting` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
> * Bật `Enable annotation processing`
> * Chọn `Obtain processors from project classpath`

---

## 5) Cấu trúc dự án chuẩn (layered structure)

```
src/
 └── main/
     ├── java/
     │   └── com/example/bookstore/
     │       ├── controller/   # REST Controllers (API layer)
     │       ├── service/      # Business logic (interfaces + impl)
     │       ├── repository/   # Spring Data JPA repositories
     │       ├── model/        # Entities
     │       ├── dto/          # Request/Response DTOs
     │       └── StudentManagementApplication.java
     └── resources/
         ├── application.properties
         └── db/migration/     # Flyway SQL scripts
```

> **Best practices**
> * **Controller** chỉ nhận/đáp DTO. **Service** xử lý business. **Repository** chỉ CRUD với Entity.
> * Sử dụng **DTO** để tách API contract khỏi Entity, tránh lộ field nội bộ.

---

## 6) Kết nối PostgreSQL (Docker/local)

### 6.1 Docker Compose (Postgres)

* Tạo file `docker-compose.yml` ở root project:

```yaml
services:
  postgres:
    image: postgres:latest
    container_name: pg-student
    environment:
      POSTGRES_DB: student_management
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 123456@root
      TZ: Asia/Ho_Chi_Minh
    ports:
      - '5432:5432'
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./docker-entrypoint-initdb.d:/docker-entrypoint-initdb.d:ro
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U postgres -d student_management" ]
      interval: 5s
      timeout: 3s
      retries: 10
      start_period: 5s

volumes:
  pgdata:
```

* Tạo file `docker-entrypoint-initdb.d/00_create_roles.sql` ở root project:

> * Đây là thư mục đặc biệt mà PostgreSQL Docker image tự động đọc các file .sql, .sql.gz, .sh khi khởi tạo lần đầu
> * `:ro` cấu hình read-only để container chỉ được đọc nội dung trong thư mục này, không được ghi (giúp tránh container ghi ngược vào máy host)
> * Note: Sẽ giải thích rõ hơn ở phần cuối module spring boot về deploy ứng dụng spring boot

```sql
-- Cấu hình Spring Boot theo mô hình 3-role

-- Create role for Flyway (DDL), NOT a superuser
CREATE ROLE flyway_user LOGIN PASSWORD '123456@flyway';

-- App user: only CRUD data
CREATE ROLE app_user LOGIN PASSWORD '123456';

-- Grant database-level permissions
GRANT CONNECT ON DATABASE student_management TO flyway_user, app_user;
GRANT CREATE  ON DATABASE student_management TO flyway_user;  -- để DDL/migrations

-- Work inside the database
\c student_management

-- Create a separate schema for the app, owned by flyway_user
CREATE SCHEMA IF NOT EXISTS app AUTHORIZATION flyway_user;

-- Restrict privileges on the public schema (hardening)
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;

-- Basic privileges on schema 'app'
GRANT USAGE ON SCHEMA app TO app_user;

-- When flyway_user creates TABLE/SEQUENCE/VIEW/FUNCTION in schema 'app',
-- automatically grant privileges to app_user (to avoid missing GRANT later)
ALTER DEFAULT PRIVILEGES FOR ROLE flyway_user IN SCHEMA app
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user;
ALTER DEFAULT PRIVILEGES FOR ROLE flyway_user IN SCHEMA app
GRANT USAGE, SELECT ON SEQUENCES TO app_user;
ALTER DEFAULT PRIVILEGES FOR ROLE flyway_user IN SCHEMA app
GRANT EXECUTE ON FUNCTIONS TO app_user;

-- (If using trusted extensions like uuid-ossp or pgcrypto via Flyway)
-- Requires CREATE EXTENSION privilege on DB; if missing, grant it to flyway_user:
GRANT CREATE ON DATABASE student_management TO flyway_user;
```

* Chạy docker để build container
```bash
  docker compose up -d
```

* Kiểm tra status container đã healthy chưa
```bash
  docker ps
```

* **Lưu ý**: Khi chạy ứng dụng Spring Boot và kết nối đến `pg-student`, nếu container trả về lỗi `FATAL: invalid value for parameter "TimeZone": "Asia/Saigon"` thì cần đặt lại timezone (system property: thuộc tính hệ thống) cho JVM (Java Virtual Machine):
  > IntelliJ IDEA → Run/Debug Configurations → VM options: `-Duser.timezone=Asia/Ho_Chi_Minh`

### 6.3 Cấu hình Spring Boot theo mô hình 3-role

`src/main/resources/application.properties`

```properties
spring.application.name=Student Management API App

api.prefix=/api/v1

# ===== Actuator =====
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=never
# Change the default base-path from /actuator to root / and include api.prefix
management.endpoints.web.base-path=${api.prefix}/

# ===== PostgreSQL DB Connection =====
spring.datasource.url=jdbc:postgresql://localhost:5432/student_management
spring.datasource.username=app_user
spring.datasource.password=123456

# ===== JPA/Hibernate =====
# Validate DB schema matches JPA entities
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# ===== Flyway migration config =====
spring.flyway.enabled=true
spring.flyway.url=jdbc:postgresql://localhost:5432/student_management
spring.flyway.user=flyway_user
spring.flyway.password=123456@flyway
spring.flyway.schemas=app
spring.flyway.default-schema=app
spring.flyway.locations=classpath:db/migration
# Check using flyway_schema_history in DB to compare the checksum of each executed migration file with the current file
spring.flyway.validate-on-migrate=true

# ===== Server =====
server.port=8080
```

---

## 7) Kiểm tra trạng thái API bằng Actuator

### 7.1 Spring Boot Actuator

> Spring Boot **Actuator** cung cấp các endpoint để theo dõi sức khỏe và trạng thái hoạt động của ứng dụng.

* Cài dependency trong **Maven**:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

* Hoặc trong **Gradle**:

```groovy
dependencies {
  implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

### 7.2 Cấu hình endpoint `/health`

> Mặc định Actuator bật `/actuator/health`.

* Có thể mở rộng bằng cách cấu hình trong `application.properties`:

```properties
# ===== Actuator =====
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=always
# Change the default base-path from /actuator to root / and include api.prefix
management.endpoints.web.base-path=${api.prefix}/
```

#### Lưu ý
> Trong môi trường `prod` thì không nên show-details trong endpoint `/health`

```properties
# ===== Actuator =====
management.endpoint.health.show-details=never
```

* Test truy cập: `http://localhost:8080/api/v1/health`
* Test thử DB down và kiểm tra response trả về

## 8) Tích hợp DevTools của Spring Boot

> Hỗ trợ tự động reload ứng dụng khi code thay đổi, giúp phát triển nhanh hơn mà không cần chạy lại thủ công

### 8.1 Cài đặt dependency

* Cài dependency trong **Maven**:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-devtools</artifactId>
  <scope>runtime</scope>
  <optional>true</optional>
</dependency>
```

* Hoặc trong **Gradle**:

```groovy
dependencies {
  developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```

### 8.2 Chỉnh Setting trong IntelliJ IDEA

#### 8.2.1 `Setting` → `Build, Execution, Deployment`
  > * Kích chọn `Compiler`
  > * Bật `Build project automatically`

#### 8.2.2 `Setting` → `Advanced Settings`
  > * Tìm mục `Compiler`
  > * Bật `Allow auto-make to start even if developed application is currently running`

* Nếu cần thiết, hãy tắt và khởi động lại IntelliJ

### 8.3 Test hoạt động của DevTools

* Tạo DemoController để test

```java
@RestController
@RequestMapping("${api.prefix}/demo")
public class DemoController {
    @GetMapping
    public ResponseEntity<String> demo() {
        return ResponseEntity.ok("Demo123");
    }
}
```

* Chạy app sẽ thấy log

```
Devtools property defaults active!
LiveReload server is running on port 35729
```

* Thử chỉnh sửa file Java và quan sát IntelliJ tự động rebuild mà không cần ấn `Rerun` 
