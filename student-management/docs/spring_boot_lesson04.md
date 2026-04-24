# Spring Boot – Buổi 4: JDBC Fundamentals

## 1) JDBC Overview

### 1.1 JDBC là gì?

> * **JDBC (Java Database Connectivity)** là chuẩn API của Java để ứng dụng Java làm việc với DB
> * Hoạt động qua **JDBC Driver** (PostgreSQL: `org.postgresql.Driver`)

#### Thành phần chính của JDBC:

| Thành phần                           | Vai trò                               |
|--------------------------------------|---------------------------------------|
| 1. `DriverManager`                   | Quản lý các driver DB và mở kết nối   |
| 2. `Connection`                      | Đại diện cho kết nối đang mở đến DB   |
| 3. `Statement` / `PreparedStatement` | Gửi câu lệnh SQL đến DB               |
| 4. `ResultSet`                       | Kết quả trả về từ câu truy vấn SELECT |

### 1.2 Luồng cơ bản (`JDBC lifecycle`)

```
Application → DriverManager → Connection → Statement → ResultSet → (map to DTO) → Application
```

1. Load driver và cấu hình từ `db.properties`
2. Lấy `Connection` từ `DriverManager.getConnection(url, user, pass)`
3. Tạo `PreparedStatement` với SQL có tham số `?`
4. Gán tham số
   * Dùng `stmt.setInt`, `stmt.setString`, `stmt.setObject` 
5. Thực thi câu SQL với `executeQuery()` hoặc `executeUpdate()`
   * `executeQuery()`: có trả về `ResultSet` (`SELECT` hoặc `INSERT` \ `UPDATE` \ `DELETE` với `RETURNING`)
   * `executeUpdate()`: không trả về `ResultSet`, chỉ trả về `int` = số dòng bị ảnh hưởng khi `INSERT` \ `UPDATE` \ `DELETE` không kèm `RETURNING` 
6. Duyệt `ResultSet`, map từng dòng → đối tượng Java
7. Đóng tài nguyên (`try‑with‑resources`)

---

## 2) DAO (Data Access Object)

Các dòng dự án Java cũ trước đây sử dụng JDBC thuần với kiến trúc
```
`Controller` → `Service` → `DAO` (JDBC thuần) → `DB`
```

> `DAO` là tầng chuyên trách giao tiếp với database, nhiệm vụ của nó là:
    >   * Kết nối đến database
>   * Thực hiện các truy vấn SQL (`SELECT`, `INSERT`, `UPDATE`, `DELETE`)
>   * Chuyển đổi kết quả từ ResultSet → đối tượng Java (model/entity)
>   * Giúp tách riêng logic truy cập dữ liệu khỏi logic nghiệp vụ của tầng `Service`

_Có thể hiểu nôm na `DAO` tương đương với tầng `Repository` của Spring Boot_

---

## 3) PreparedStatement & ResultSet Handling

### 3.1 PreparedStatement

> `PreparedStatement` giúp gửi câu SQL có tham số `?`
>   * **Bảo mật**: tránh SQL Injection nhờ tham số `?`
>   * **Hiệu năng**: DB có thể cache plan cho câu lệnh đã chuẩn bị
>   * **An toàn kiểu**: gán kiểu dữ liệu rõ ràng (`setString`, `setInt`, `setObject`, ...)

### 3.2 ResultSet

> `ResultSet` hoạt động như một con trỏ, duyệt từng hàng dữ liệu từ DB

| Phương thức            | Mô tả                                                    |
|------------------------|----------------------------------------------------------|
| `rs.next()`            | Di chuyển đến hàng kế tiếp (trả `false` khi hết dữ liệu) |
| `rs.getInt(column)`    | Lấy giá trị `int` từ cột                                 |
| `rs.getString(column)` | Lấy giá trị `String`                                     |
| `rs.getDate(column)`   | Lấy giá trị `Date`                                       |

### 3.3 Ví dụ PreparedStatement & ResultSet Handling

```java
public class StudentDao {
    public Optional<Student> findByEmail(String email) {
        String sql = "SELECT * FROM app.students WHERE email = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                (UUID) rs.getObject("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                (Integer) rs.getObject("age"),
                rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC).toInstant()
        );
    }
}
```

> * `?` là placeholder để gán giá trị `email` thật vào sau → giúp chống `SQL injection`
>   * SQL không an toàn: `SELECT id, full_name FROM app.students WHERE email = '" + email + "'`
>   * Hacker tấn công `SQL injection` bằng cách truyền `a@example.com' OR '1'='1`
>   * `'1'='1'` luôn đúng → mệnh đề WHERE luôn `true` → query trả về tất cả rows → lộ dữ liệu không mong muốn
> * Sử dụng `try-with-resource` để tự động đóng kết nối sau khi dùng xong
>   * `DB.getConnection()` lấy kết nối PostgreSQL
>   * `prepareStatement(sql)` chuẩn bị câu SQL ở trên để thực thi
> * Dùng `stmt.setInt`, `stmt.setString`, `stmt.setObject` để gán giá trị cho `?`
>   * `ps.setString(1, email)`: gán giá trị email thật vào dấu `?` đầu tiên trong câu SQL
>   * 1 là vị trí tham số thứ nhất, vì có thể có nhiều `?` trong câu SQL
> * `try (ResultSet rs = ps.executeQuery())`: thực thi câu lệnh SELECT và kết quả trả về nằm trong `ResultSet rs`
> * `rs.next()`: di chuyển con trỏ tới dòng đầu tiên, nếu có dữ liệu thì trả về `true`

### 3.4 Câu lệnh INSERT / UPDATE / DELETE

```java
public class StudentDao {
    public Student save(String fullName, String email, Integer age) {
        String insertSql = """
                INSERT INTO app.students(full_name, email, age)
                VALUES (?, ?, ?)
                RETURNING id, full_name, email, age, created_at
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql)
        ) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            if (age == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, age);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }
}
```

> * Sử dụng `RETURNING` để trả về kết quả cho `ResultSet`

---

## 4) Nhược điểm của JDBC thuần

| Vấn đề                             | Mô tả                                                                                              |
|------------------------------------|----------------------------------------------------------------------------------------------------|
| **Mã lặp lại nhiều**               | Cần nhiều code để mở, đóng, gán tham số, đọc dữ liệu                                               |
| **Không ánh xạ Object-Relational** | Cần tự viết chuyển đổi giữa Object ↔ Row (`mapRow()`)<br/> → Dễ lỗi khi tên cột / kiểu dữ liệu đổi |
| **Khó mở rộng & bảo trì**          | SQL rải rác khắp nơi<br/> → Khi bảng thay đổi, phải cập nhật thủ công câu SQL nhiều nơi            |

> Trong các buổi tiếp theo, sẽ học `Spring Data JPA` để giải quyết các hạn chế này
>   * `Spring JdbcTemplate`: giảm lặp code, quản lý mở-đóng resource/exception tốt hơn
>   * `JPA/Hibernate`: ORM, map entity ↔ table, query với JPQL/Criteria, migration Flyway

---

## 5) Thực hành: Kết nối PostgreSQL thật với JDBC thuần

### 5.1 Cấu trúc thư mục

```
jdbc-demo/
├─ pom.xml
├─ README.md
├─ src/
│  ├─ main/java/
│  │  └─ demo/jdbc/
│  │     ├─ App.java
│  │     ├─ db/DB.java
│  │     ├─ dao/StudentDao.java
│  │     └─ model/Student.java
│  └─ main/resources/
│     ├─ db.properties
│     └─ init.sql
```

### 5.2 Cài đặt môi trường

#### 5.2.1 `pom.xml` (Maven)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>demo</groupId>
    <artifactId>jdbc-demo</artifactId>
    <version>1.0.0</version>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- PostgreSQL JDBC driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.7</version>
        </dependency>

        <!-- SparkJava: web microframework -->
        <dependency>
            <groupId>com.sparkjava</groupId>
            <artifactId>spark-core</artifactId>
            <version>2.9.4</version>
            <exclusions>
                <exclusion>
                    <groupId>org.eclipse.jetty</groupId>
                    <artifactId>jetty-server</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.eclipse.jetty</groupId>
                    <artifactId>jetty-webapp</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.eclipse.jetty.websocket</groupId>
                    <artifactId>websocket-server</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.eclipse.jetty.websocket</groupId>
                    <artifactId>websocket-client</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.eclipse.jetty</groupId>
            <artifactId>jetty-server</artifactId>
            <version>9.4.58.v20250814</version>
        </dependency>
        <dependency>
            <groupId>org.eclipse.jetty</groupId>
            <artifactId>jetty-webapp</artifactId>
            <version>9.4.58.v20250814</version>
        </dependency>
        <dependency>
            <groupId>org.eclipse.jetty.websocket</groupId>
            <artifactId>websocket-server</artifactId>
            <version>9.4.58.v20250814</version>
        </dependency>
        <dependency>
            <groupId>org.eclipse.jetty.websocket</groupId>
            <artifactId>websocket-client</artifactId>
            <version>9.4.58.v20250814</version>
        </dependency>

        <!-- Gson: JSON serializer -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.11.0</version>
        </dependency>

        <!-- Logging: slf4j-api 1.7.36 (stable) -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.36</version>
        </dependency>

        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>1.7.36</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Plugin: run class contained main() method -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.5.0</version>
                <configuration>
                    <mainClass>demo.jdbc.App</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 5.2.2 Cấu hình `db.properties`

```properties
# src/main/resources/db.properties

db.url=jdbc:postgresql://localhost:5432/jdbc_demo?currentSchema=app
db.username=postgres
db.password=123456@root
db.schema=app
```

### 5.3 Khởi tạo Postgres DB và init schema

* Khởi tạo DB `jdbc_demo` theo cấu hình `db.properties` 
* Chạy script để init schema và seed dữ liệu mock

```sql
-- src/main/resources/init.sql

CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.students(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- using pgcrypto extension
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(200) UNIQUE NOT NULL,
    age INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO app.students(full_name, email, age)
VALUES
('Nguyễn Văn A', 'a@example.com', 20),
('Trần Thị B',  'b@example.com', 22),
('Lê Văn C',    'c@example.com', 21)
ON CONFLICT (email) DO NOTHING;
```

---

### 5.4 Class kết nối DB

Nhiệm vụ của class `DB`:
* Đọc thông tin kết nối CSDL (PostgreSQL) từ file cấu hình `db.properties`
* Load driver JDBC
* Cung cấp hàm tiện ích `getConnection()` để mở kết nối tới database

```java
// src/main/java/demo/jdbc/db/DB.java

public final class DB {
    private static final Properties props = new Properties();
    private static final String url;
    private static final String username;
    private static final String password;

    static {
        try (InputStream input = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) throw new IllegalStateException("Không tìm thấy file db.properties");
            props.load(input);
            url = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");

            Class.forName("org.postgresql.Driver");
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc db.properties: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Không tìm thấy PostgreSQL JDBC Driver", e);
        }
    }

    private DB() {}

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new RuntimeException("Không kết nối được DB: " + e.getMessage(), e);
        }
    }
}
```

> * `Khối static {}`: 
>   * Được chạy 1 lần duy nhất khi chương trình chạm tới class DB lần đầu (ví dụ khi gọi `DB.getConnection()`)
>   * Gán giá trị ban đầu cho các biến `static final` của class
> * `DB.class.getClassLoader().getResourceAsStream("db.properties")`: tìm và mở file `db.properties` trong thư mục `resources/`
> * `props.load(input)`: load toàn bộ nội dung file để gán vào đối tượng Properties
> * `Class.forName("org.postgresql.Driver")`: load driver JDBC PostgreSQL
> * `DriverManager.getConnection(url, username, password)`:
>   * Mở kết nối tới PostgreSQL
>   * Trả về đối tượng `Connection`, từ đó có thể thực hiện truy vấn SQL (`SELECT`, `INSERT`, …)

#### Bài tập 1: Viết class `DB` hoàn chỉnh

```java
// src/main/java/demo/jdbc/db/DB.java

public final class DB {
    private static final Properties props = new Properties();
    private static final String url;
    private static final String username;
    private static final String password;

    static {
        try (InputStream input = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
            // Hãy hoàn thiện code
        } catch (IOException e) {
            // Hãy hoàn thiện code
        } catch (ClassNotFoundException e) {
            // Hãy hoàn thiện code
        }
    }

    private DB() {}

    public static Connection getConnection() {
        // Hãy hoàn thiện code
    }
}
```

### 5.5 Tạo Model `Student`

```java
// src/main/java/demo/jdbc/model/Student.java

public record Student(
        UUID id,
        String fullName,
        String email,
        Integer age,
        Instant createdAt) {}
```

### 5.6 Tạo DAO `StudentDao`

```java
// src/main/java/demo/jdbc/dao/StudentDao.java

public class StudentDao {
    public List<Student> findAll() {
        String sql = """
                SELECT id, full_name, email, age, created_at
                FROM app.students
                ORDER BY created_at DESC
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()
        ) {
            List<Student> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public Optional<Student> findById(UUID id) {
        String sql = """
                SELECT id, full_name, email, age, created_at
                FROM app.students
                WHERE id = ?
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public Optional<Student> findByEmail(String email) {
        String sql = """
                SELECT id, full_name, email, age, created_at
                FROM app.students
                WHERE email = ?
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM app.students WHERE email = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public Student save(String fullName, String email, Integer age) {
        String sql = """
                INSERT INTO app.students(full_name, email, age)
                VALUES (?, ?, ?)
                RETURNING id, full_name, email, age, created_at
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            if (age == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, age);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public Student update(UUID id, String fullName, Integer age) {
        String sql = """
                UPDATE app.students
                SET full_name = ?, age = ?
                WHERE id = ?
                RETURNING id, full_name, email, age, created_at
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, fullName);
            if (age == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, age);
            ps.setObject(3, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new RuntimeException("Not found student with id: " + id);
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM app.students WHERE id = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setObject(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                (UUID) rs.getObject("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                (Integer) rs.getObject("age"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
```

#### Bài tập 2: Viết class `StudentDao` hoàn chỉnh

```java
// src/main/java/demo/jdbc/dao/StudentDao.java

public class StudentDao {
    private Student mapRow(ResultSet rs) throws SQLException {
        // Hãy hoàn thiện code
    }
    
    public List<Student> findAll() {
        String sql = """
                Hãy viết câu SQL SELECT
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()
        ) {
            // Hãy hoàn thiện code
        } catch (SQLException e) {
            // Hãy hoàn thiện code
        }
    }

    public Optional<Student> findById(UUID id) {
        // Hãy hoàn thiện code
    }

    public Optional<Student> findByEmail(String email) {
        // Hãy hoàn thiện code
    }

    public boolean existsByEmail(String email) {
        // Hãy hoàn thiện code
    }

    public Student save(String fullName, String email, Integer age) {
        // Hãy hoàn thiện code
    }

    public Student update(UUID id, String fullName, Integer age) {
        // Hãy hoàn thiện code
    }

    public boolean deleteById(UUID id) {
        // Hãy hoàn thiện code
    }
}
```

### 5.7 Demo RESTfull API với JDBC thuần

```java
// src/main/java/demo/jdbc/App.java

public class App {
    public static void main(String[] args) {
        port(8080);

        // Middleware: JSON & CORS
        after((req, res) -> res.type("application/json"));
        options("/*", (req, res) -> {
            String reqHeaders = req.headers("Access-Control-Request-Headers");
            if (reqHeaders != null) res.header("Access-Control-Allow-Headers", reqHeaders);
            String reqMethod = req.headers("Access-Control-Request-Method");
            if (reqMethod != null) res.header("Access-Control-Allow-Methods", reqMethod);
            return "OK";
        });
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");

            System.out.println(">>> " + req.requestMethod() + " " + req.uri()
                    + (req.raw().getQueryString() != null ? "?" + req.raw().getQueryString() : ""));
            System.out.println("Query keys = " + req.queryParams());
            for (String k : req.queryParams()) {
                System.out.println(" - " + k + " = [" + req.queryParams(k) + "]");
            }
        });

        // Healthcheck
        get("/health", (req, res) -> JsonUtil.toJson(Map.of("ok", true)));

        StudentDao dao = new StudentDao();

        // === CRUD - REST API via Spark ===

        // Get all with filter by email
        get("/students", (req, res) -> {
            String email = req.queryParams("email");

            if (email != null && !email.isBlank()) {
                return dao.findByEmail(email)
                        .<Object>map(JsonUtil::toJson)
                        .orElseGet(() -> {
                            res.status(404);
                            return JsonUtil.toJson(Map.of("error", "Not found"));
                        });
            }
            // fallback
            var list = dao.findAll();
            return JsonUtil.toJson(list);
        });

        // Get by id
        get("/students/:id", (req, res) -> {
            try {
                UUID id = UUID.fromString(req.params(":id"));
                return dao.findById(id)
                        .<Object>map(JsonUtil::toJson)
                        .orElseGet(() -> {
                            res.status(404);
                            return JsonUtil.toJson(Map.of("error", "Not found"));
                        });
            } catch (IllegalArgumentException ex) {
                res.status(400);
                return JsonUtil.toJson(Map.of("error", "Invalid UUID"));
            }
        });

        // Create
        post("/students", (req, res) -> {
            var body = JsonUtil.fromJson(req.body(), StudentCreateRequest.class);

            // Validate
            List<String> errors = new ArrayList<>();
            if (body.fullName == null || body.fullName.isBlank()) errors.add("fullName is required");
            if (body.email == null || !body.email.contains("@")) errors.add("email is invalid");
            if (body.age == null || body.age < 16) errors.add("age must be >= 16");
            if (!errors.isEmpty()) {
                res.status(400);
                return JsonUtil.toJson(Map.of("errors", errors));
            }

            if (dao.existsByEmail(body.email)) {
                res.status(400);
                return JsonUtil.toJson(Map.of("error", "email already exists"));
            }

            Student created = dao.save(body.fullName, body.email, body.age);
            res.status(201);

            String prefix = "http://localhost:8080";
            res.header("Location", prefix + "/students/" + created.id());

            return JsonUtil.toJson(created);
        });

        // Update
        put("/students/:id", (req, res) -> {
            UUID id;
            try {
                id = UUID.fromString(req.params(":id"));
            } catch (IllegalArgumentException ex) {
                res.status(400);
                return JsonUtil.toJson(Map.of("error", "Invalid UUID"));
            }

            var body = JsonUtil.fromJson(req.body(), StudentUpdateRequest.class);

            List<String> errors = new ArrayList<>();
            if (body.fullName == null || body.fullName.isBlank()) errors.add("fullName is required");
            if (body.age == null || body.age < 16) errors.add("age must be >= 16");
            if (!errors.isEmpty()) {
                res.status(400);
                return JsonUtil.toJson(Map.of("errors", errors));
            }

            if (dao.findById(id).isEmpty()) {
                res.status(404);
                return JsonUtil.toJson(Map.of("error", "Not found"));
            }

            Student updated = dao.update(id, body.fullName, body.age);
            return JsonUtil.toJson(updated);
        });

        // Delete
        delete("/students/:id", (req, res) -> {
            try {
                UUID id = UUID.fromString(req.params(":id"));
                boolean ok = dao.deleteById(id);
                if (!ok) {
                    res.status(404);
                    return JsonUtil.toJson(Map.of("error", "Not found"));
                }
                res.status(204);
                return "";
            } catch (IllegalArgumentException ex) {
                res.status(400);
                return JsonUtil.toJson(Map.of("error", "Invalid UUID"));
            }
        });

        // Exception fallback
        exception(Exception.class, (e, req, res) -> {
            res.type("application/json");
            res.status(500);
            res.body(JsonUtil.toJson(Map.of("error", "Internal Server Error", "message", e.getMessage())));
        });
    }
}
```

## 6) Chạy thử và kiểm tra bằng Postman

* Thử tạo mới, cập nhật, xóa và đọc danh sách
