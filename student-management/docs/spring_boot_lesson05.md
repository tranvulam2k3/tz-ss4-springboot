# Spring Boot – Buổi 5: ORM & Hibernate Introduction

## 1) ORM là gì?

### 1.1 Khái niệm ORM

> **ORM (Object Relational Mapping)** là kỹ thuật ánh xạ (chuyển đổi dữ liệu) giữa `object trong Java` và `bảng trong database` một cách tự động
>
> Thay vì viết SQL thủ công (như trong JDBC thuần), ORM cho phép thao tác bằng `object`

Ví dụ:

* Với JDBC thuần phải viết:

```java
public Student save(String fullName, String email, Integer age) {
    String sql = "INSERT INTO students(full_name, email, age) VALUES (?, ?, ?)";
}
```

* Với ORM chỉ cần:

```java
public Student save(String fullName, String email, Integer age) {
    Student s = new Student("Nguyen Van A", 20, "vana@gmail.com");
    session.persist(s);    
}
```

→ Hibernate sẽ tự động tạo câu SQL tương ứng:

```sql
INSERT INTO students(full_name, age, email) VALUES ('Nguyen Van A', 20, 'vana@gmail.com');
```

### 1.2 Lợi ích của ORM

| Lợi ích                | Mô tả                                                                  |
|------------------------|------------------------------------------------------------------------|
| Giảm code SQL thủ công | Không cần viết nhiều câu lệnh JDBC                                     |
| Tự ánh xạ Object–Table | Entity ↔ Table, Field ↔ Column                                         |
| Dễ bảo trì             | Khi cấu trúc bảng thay đổi, chỉ cần cập nhật Entity                    |
| An toàn hơn            | Tránh SQL injection                                                    |
| Đa hệ CSDL             | Có thể đổi DB dễ dàng (MySQL, PostgreSQL, H2, …) mà không cần đổi code |
| Tích hợp transaction   | Hibernate tự quản lý commit/rollback                                   |

---

## 2) Hibernate

### 2.1 Khái niệm

> Hibernate là một `ORM framework` phổ biến nhất trong hệ sinh thái Java, gồm các tính năng:
> * Quản lý ánh xạ Java class ↔ table trong DB
> * Tự động sinh SQL
> * Quản lý Transaction

### 2.2 Các thành phần chính của Hibernate

| Thành phần       | Vai trò                                                            |
|------------------|--------------------------------------------------------------------|
| `Configuration`  | Nạp file cấu hình `hibernate.cfg.xml` và khởi tạo `SessionFactory` |
| `SessionFactory` | Tạo và quản lý các `Session`                                       |
| `Session`        | Đại diện cho kết nối (connection) đến DB, dùng để `CRUD entity`    |
| `Transaction`    | Quản lý `commit` / `rollback`                                      |
| `Entity`         | Class ánh xạ với bảng                                              |

### 2.3 Luồng hoạt động Hibernate

```
Hibernate config → SessionFactory → Session → Transaction → CRUD → Commit/Rollback → Close Session
```

---

## 3) JPA (Java Persistence API)

### 3.1 Khái niệm

> * Là chuẩn ORM trong Java
> * Định nghĩa các annotation/entity, cách quản lý dữ liệu giữa Object ↔ Database
> * JPA không tự giao tiếp DB mà cần Hibernate (hoặc ORM framework khác) để triển khai

### 3.2 Annotation JPA phổ biến

#### 3.2.1 Annotation cho Entity

| Annotation             | Mục đích                             |
|------------------------|--------------------------------------|
| `@Entity`              | Định nghĩa 1 lớp là Entity trong ORM |
| `@Table(...)`          | Ánh xạ Entity với tên bảng trong DB  |
| `@Id`                  | Đánh dấu khóa chính                  |
| `@GeneratedValue(...)` | Tự sinh giá trị ID                   |

Ví dụ:

```java
@Entity
@Table(name = "students", schema = "app")
public class StudentEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
}
```

#### 3.2.2 Annotation cho cột

| Annotation                             | Mục đích                                                         |
|----------------------------------------|------------------------------------------------------------------|
| `@Column(...)`                         | Tùy chỉnh column trong DB                                        |
| `@Lob`                                 | Kiểu dữ liệu lớn (TEXT, BLOB)                                    |
| `@Transient`                           | Không lưu xuống DB                                               |
| `@Enumerated(EnumType.STRING/ORDINAL)` | Lưu enum trong DB                                                |
| `@Temporal`                            | Chỉ định kiểu thời gian (DATE/TIME/TIMESTAMP) cho java.util.Date |

Ví dụ:

```java
@Column(name = "email", nullable = false, unique = true, length = 200)
private String email;

@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
private Instant createdAt;
```

#### 3.2.3 Annotation cho quan hệ giữa các Entity

| Annotation              | Kiểu quan hệ            |
|-------------------------|-------------------------|
| `@OneToOne`             | 1–1                     |
| `@OneToMany`            | 1–N                     |
| `@ManyToOne`            | N–1                     |
| `@ManyToMany`           | N–N                     |
| `@JoinColumn(name=...)` | Khóa ngoại              |
| `@JoinTable(...)`       | Bảng trung gian cho N–N |

Ví dụ quan hệ 1–N:

```java
@OneToMany(mappedBy = "student")
private List<Enrollment> enrollments;
```

#### 3.2.4 Annotation cho lifecycle (vòng đời Entity)

| Annotation     | Khi nào chạy       |
|----------------|--------------------|
| `@PrePersist`  | Trước khi insert   |
| `@PostPersist` | Sau khi insert     |
| `@PreUpdate`   | Trước khi update   |
| `@PostUpdate`  | Sau khi update     |
| `@PreRemove`   | Trước khi delete   |
| `@PostRemove`  | Sau khi delete     |
| `@PostLoad`    | Sau khi load từ DB |

Ví dụ set timestamp:

```java
@PrePersist
public void prePersist() {
  createdAt = LocalDateTime.now();
}
```

#### 3.2.5 Annotation cho caching / fetch

| Annotation                             | Mục đích                                                         |
|----------------------------------------|------------------------------------------------------------------|
| `@Basic(fetch = LAZY/EAGER)`           | Cách fetch mặc định                                              |
| `@NamedQuery`, `@NamedNativeQuery`     | Định nghĩa query cố định                                         |

---

## 3) Entity Mapping cơ bản

### 3.1 Thêm dependencies cho Hibernate

```xml
<!-- Hibernate (ORM) -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.6.1.Final</version>
</dependency>
<dependency>
    <groupId>jakarta.persistence</groupId>
    <artifactId>jakarta.persistence-api</artifactId>
    <version>3.1.0</version>
</dependency>
```

### 3.2 Cấu hình `hibernate.cfg.xml`

```xml
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
        "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
    <session-factory>
        <!-- JDBC -->
        <property name="hibernate.connection.driver_class">org.postgresql.Driver</property>
        <property name="hibernate.connection.url">jdbc:postgresql://localhost:5432/jdbc_demo?currentSchema=app</property>
        <property name="hibernate.connection.username">postgres</property>
        <property name="hibernate.connection.password">123456@root</property>

        <!-- Show SQL -->
        <property name="hibernate.show_sql">true</property>
        <property name="hibernate.format_sql">true</property>

        <!-- Auto DDL: none -->
        <property name="hibernate.hbm2ddl.auto">none</property>

        <!-- Declare entity -->
        <mapping class="demo.jdbc.model.orm.StudentEntity"/>
    </session-factory>
</hibernate-configuration>
```

**Lưu ý:**
* Hành vi Hibernate thực thi Auto DDL:
  * `validate`: hibernate sẽ so sánh shema giữa entity và DB, nếu khác → báo lỗi
  * `update`: hibernate tự sửa bảng ở DB cho khớp entity (ALTER TABLE) → chỉ nên dùng trong môi trường Dev
  * `none`: hibernate không tự ý sửa shema DB
* Thẻ `<mapping class="demo.jdbc.model.orm.StudentEntity"/>` cần khai báo chính xác tên và source root của entity

### 3.3 Thêm cột updated_at cho DB

* Chạy script để thêm cột updated_at

```sql
ALTER TABLE app.students
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
```

* Chạy script để tạo trigger tự động gán updated_at mỗi lần thực hiện `UPDATE`

```sql
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_set_updated_at
BEFORE UPDATE ON app.students
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
```

**Lưu ý:** Nếu không muốn dùng pgAdmin thì có thể dùng `psql` trong container `pg-student`

* Mở terminal bên trong container `pg-student`

```bash
  docker exec -it pg-student bash
```

* Vào `psql`

```bash
  psql - U postgres
```

* Chạy các lệnh `ALTER TABLE...;` `CREATE OR REPLACE FUNCTION...;` `CREATE TRIGGER...;` ở trên

* Kiểm tra kết quả trên `psql`

```bash
  \d app.students
```

### 3.3 Tạo model `StudentEntity`

```java
// demo/jdbc/model/orm/StudentEntity.java

@Entity
@Table(name = "students", schema = "app")
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "age")
    private int age;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    // Getter/Setter
    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
```

#### Giải thích Annotation

> * `@Entity`: đánh dấu class là entity sẽ ánh xạ tới table trong DB
> * `@Table`: khai báo tên bảng và schema
> * `@Id`: xác định cột khóa chính
> * `@GeneratedValue(strategy = GenerationType.UUID)`: cấu hình để hibernate sinh ID tự động
> * `@Column`: khai báo cho cột `name`, `nullable`, `unique`, `length`, ...
>   * `insertable = false`: hibernate không đưa created_at, updated_at vào câu SQL INSERT
>     * `insert into app.students (id, full_name, email, age) values (?, ?, ?, ?)`
>   * `updatable = false`: hibernate không đưa created_at, updated_at vào câu SQL UPDATE
> * `@PrePersist`, `@PreUpdate`: hibernate tự động gán thời gian tạo/cập nhật
>   * Thông thường nên để DB tự lo gán thời gian tạo/cập nhật (đối với UPDATE thì dùng trigger)
>   * Khi update bằng tool khác không phải hibernate thì vẫn an toàn, không lo sót dữ liệu vì DB đã tự lo  

#### Bài tập 1: Viết model `StudentEntity` tương ứng với bảng students

```java
// demo/jdbc/model/orm/StudentEntity.java

// Hãy khai báo Annotation cần thiết
public class StudentEntity {

    // Hãy khai báo và cấu hình cho các cột 

    // Hãy viết các Getter/Setter cần thiết
}
```

---

## 4) Hibernate Session & Transaction

### 4.1 Build SessionFactory

* Hibernate cần SessionFactory để tạo ra Session
* Đại diện cho kết nối đến DB, dùng để `CRUD entity`

```java
public class HibernateUtil {
  private static final SessionFactory sessionFactory = buildSessionFactory();

  private static SessionFactory buildSessionFactory() {
    try {
      Configuration config = new Configuration().configure();
      System.out.println(">>> Starting... building SessionFactory");
      return config.buildSessionFactory();
    } catch (Exception e) {
      throw new RuntimeException("Failed to build SessionFactory", e);
    }
  }

  private HibernateUtil() {}

  public static SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public static void closeSession() {
    sessionFactory.close();
    System.out.println(">>> Shutting down... closed SessionFactory");
  }
}
```

**Lưu ý**: `config.buildSessionFactory()` thực hiện nhiều tác vụ nặng để build `SessionFactory`
* Suốt vòng đời app chỉ nên tạo 1 instance `SessionFactory` duy nhất để dùng chung (Singleton pattern)
* Sử dụng biến static cho `SessionFactory sessionFactory` → tạo duy nhất 1 lần khi load class HibernateUtil

> `new Configuration().configure()`: đọc file `hibernate.cfg.xml` để `config.buildSessionFactory()` thực hiện:
>   * kết nối DB
>   * load mapping entity
>   * khởi tạo engine ORM của Hibernate

### 4.2 Mở Session và lưu entity

```java
// demo/jdbc/dao/HibernateStudentDao.java

public class HibernateStudentDao {
  public StudentEntity save(String fullName, String email, Integer age) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      transaction = session.beginTransaction();

      StudentEntity student = new StudentEntity();
      student.setFullName(fullName);
      student.setEmail(email);
      student.setAge(age);

      session.persist(student);
      session.flush();
      session.refresh(student);

      transaction.commit();
      return student;
    } catch (Exception e) {
      if (transaction != null) transaction.rollback();
      throw e;
    }
  }
}
```

#### Giải thích luồng hoạt động

| Bước                                   | Vai trò                                                                                      |
|----------------------------------------|----------------------------------------------------------------------------------------------|
| 1. `sessionFactory.openSession()`      | Mở `Session` – tạo kết nối làm việc cụ thể                                                   |
| 2. `session.beginTransaction()`        | Bắt đầu Transaction – làm việc với Entity, thực hiện CRUD                                    |
| 2.1 `session.persist(student)`         | Hibernate tự sinh SQL INSERT                                                                 |
| 2.2 `session.flush()`                  | Đẩy SQL INSERT xuống DB để insert bản ghi trên transaction của DB                            |
| 2.3 `session.refresh(student)`         | Sinh SQL SELECT để lấy dữ liệu sau khi INSERT (`created_at/updated_at = now()`)              |
| 3. `transaction.commit()/rollback()`   | Kết thúc transaction, nếu có lỗi → rollback, nếu không → commit để ghi dữ liệu INSERT vào DB |

> Hibernate chuyển Object thành SQL INSERT/UPDATE/DELETE tương ứng

**Lưu ý**:
* Ở class `StudentEntity` cần Hibernate tự động sinh id, nếu không Hibernate sẽ insert vào bảng students ở DB với id là null, gây xung đột DB:
  * Hibernate ném lỗi `LogicalConnectionManagedImpl … is closed` khi cố thực hiện `session.persist(student)`

### 4.3 Tạo `HibernateStudentDao` song song với `StudentDao` (JDBC) để so sánh

```java
// demo/jdbc/orm/HibernateUtil.java

public class HibernateStudentDao {
  public List<StudentEntity> findAll() {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      // Hibernate Query Language (HQL)
      final Query<StudentEntity> query = session.createQuery(
              "from StudentEntity s order by s.createdAt desc", StudentEntity.class
      );

      // Execute query and get a list of StudentEntity
      return query.getResultList();
    }
  }

  public Optional<StudentEntity> findById(UUID id) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      // session.get(): only using for primary key
      return Optional.ofNullable(session.get(StudentEntity.class, id));
    }
  }

  public Optional<StudentEntity> findByEmail(String email) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      final Query<StudentEntity> query = session.createQuery(
              "from StudentEntity s where s.email = :email", StudentEntity.class
      );
      query.setParameter("email", email);

      return query.uniqueResultOptional();
    }
  }

  public boolean existsByEmail(String email) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      final Query<Long> query = session.createQuery(
              "select count(s) from StudentEntity s where s.email = :email", Long.class
      );
      query.setParameter("email", email);

      // query.getSingleResult(): dùng khi kết quả query DB chắc chắn trả về ít nhất 1 bản ghi, ví dụ select count()
      return query.getSingleResult() > 0;
    }
  }

  public StudentEntity save(String fullName, String email, Integer age) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      transaction = session.beginTransaction();

      StudentEntity student = new StudentEntity();
      student.setFullName(fullName);
      student.setEmail(email);
      student.setAge(age);

      session.persist(student);
      session.flush();
      session.refresh(student);

      transaction.commit();
      return student;
    } catch (Exception e) {
      if (transaction != null) transaction.rollback();
      throw e;
    }
  }

  public StudentEntity update(UUID id, String fullName, Integer age) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      transaction = session.beginTransaction();

      StudentEntity student = session.get(StudentEntity.class, id);
      if (student == null) throw new RuntimeException("Not found student with id: " + id);

      student.setFullName(fullName);
      student.setAge(age);

      session.flush();
      session.refresh(student);

      transaction.commit();
      return student;
    } catch (Exception e) {
      if (transaction !=null) transaction.rollback();
      throw e;
    }
  }

  public boolean deleteById(UUID id) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      transaction = session.beginTransaction();

      StudentEntity student = session.get(StudentEntity.class, id);
      if (student == null) {
        transaction.rollback();
        return false;
      }
      session.remove(student);
      transaction.commit();
      return true;
    } catch (Exception e) {
      if (transaction != null) transaction.rollback();
      throw e;
    }
  }
}
```

> * `session.createQuery("from StudentEntity s order by s.createdAt desc", StudentEntity.class)`
>   * Sử dụng Hibernate Query Language (HQL): viết query với Java Object thay vì với table 
>   * `.createQuery()`: cần truyền class trong câu HQL, ở đây là `StudentEntity`
> * `query.getResultList()`: thực thi query và lấy danh sách `List<StudentEntity>`
> * `session.get(StudentEntity.class, id)`: lấy `StudentEntity` ứng với id truyền vào
>   * `session.get()`: chỉ dùng cho cột khóa chính
> * `query.uniqueResultOptional()`: thực thi query và lấy `StudentEntity` duy nhất (nếu có)
>   * Phù hợp khi cột `email` có ràng buộc `unique`
>   * Số row trả về từ DB:
>     * `0 row`: Optional.empty()
>     * `1 row`: Optional.of(result)
>     * `> 1 row`: ném `NonUniqueResultException`
> * `query.getSingleResult()`: dùng khi kết quả query DB chắc chắn trả về **ít nhất 1 bản ghi**, ví dụ select count()

#### Bài tập 2: Viết class `HibernateStudentDao` hoàn chỉnh

```java
// demo/jdbc/dao/HibernateStudentDao.java

public class HibernateStudentDao {
  public List<StudentEntity> findAll() {
    // Hãy hoàn thiện code
  }

  public Optional<StudentEntity> findById(UUID id) {
    // Hãy hoàn thiện code
  }

  public Optional<StudentEntity> findByEmail(String email) {
    // Hãy hoàn thiện code
  }

  public boolean existsByEmail(String email) {
    // Hãy hoàn thiện code
  }

  public StudentEntity save(String fullName, String email, Integer age) {
    // Hãy hoàn thiện code
  }

  public StudentEntity update(UUID id, String fullName, Integer age) {
    // Hãy hoàn thiện code
  }

  public boolean deleteById(UUID id) {
    // Hãy hoàn thiện code
  }
}
```

---

## 5) So sánh JDBC vs Hibernate ORM

| Tiêu chí               | JDBC                      | ORM (Hibernate)                  |
|------------------------|---------------------------|----------------------------------|
| Thao tác CRUD          | Bằng SQL                  | Bằng Object                      |
| Viết SQL thủ công      | Có                        | Không cần, Hibernate tự sinh     |
| Mapping Object ↔ Table | Tự viết map ResultSet     | Tự động ánh xạ Object-Table      |
| Transaction            | Thủ công                  | Hibernate tự quản lý             |
| SQL Injection          | Dễ mắc lỗi nếu nối chuỗi  | An toàn hơn (parameter binding)  |

---

## 6) Demo RESTfull API với Hibernate ORM

* Viết thêm route “ORM song song” `/orm/students`, vẫn giữ nguyên route cũ `/students` (JDBC thuần)

```java
// demo/jdbc/App.java

public class App {
  public static void main(String[] args) {
    port(8080);

    // Register shutdown hook (when app stop) for closing SessionFactory
    Runtime.getRuntime().addShutdownHook(new Thread(HibernateUtil::closeSession));

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
    String localhost = "http://localhost:8080";

    // === CRUD - REST API via Spark (pure JDBC) ===

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

      res.header("Location", localhost + "/students/" + created.id());

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


    // === CRUD via Hibernate ORM ===
    HibernateStudentDao hdao = new HibernateStudentDao();

    // Get all / filter by email
    get("/orm/students", (req, res) -> {
      String email = req.queryParams("email");
      if (email != null && !email.isBlank()) {
        return hdao.findByEmail(email)
                .<Object>map(JsonUtil::toJson)
                .orElseGet(() -> {
                  res.status(404);
                  return JsonUtil.toJson(Map.of("error","Not found"));
                });
      }
      var list = hdao.findAll();
      return JsonUtil.toJson(list);
    });

    // Get by id
    get("/orm/students/:id", (req, res) -> {
      try {
        UUID id = UUID.fromString(req.params(":id"));
        return hdao.findById(id)
                .<Object>map(JsonUtil::toJson)
                .orElseGet(() -> {
                  res.status(404);
                  return JsonUtil.toJson(Map.of("error","Not found"));
                });
      } catch (IllegalArgumentException ex) {
        res.status(400);
        return JsonUtil.toJson(Map.of("error","Invalid UUID"));
      }
    });

    // Create
    post("/orm/students", (req, res) -> {
      var body = JsonUtil.fromJson(req.body(), StudentCreateRequest.class);

      List<String> errors = new ArrayList<>();
      if (body.fullName == null || body.fullName.isBlank()) errors.add("fullName is required");
      if (body.email == null || !body.email.contains("@")) errors.add("email is invalid");
      if (body.age == null || body.age < 16) errors.add("age must be >= 16");
      if (!errors.isEmpty()) { res.status(400); return JsonUtil.toJson(Map.of("errors", errors)); }

      if (hdao.existsByEmail(body.email)) { res.status(400); return JsonUtil.toJson(Map.of("error", "email already exists")); }

      StudentEntity created = hdao.save(body.fullName, body.email, body.age);
      res.status(201);
      res.header("Location", localhost + "/orm/students/" + created.getId());
      return JsonUtil.toJson(created);
    });

    // Update
    put("/orm/students/:id", (req, res) -> {
      UUID id;
      try { id = UUID.fromString(req.params(":id")); }
      catch (IllegalArgumentException ex) { res.status(400); return JsonUtil.toJson(Map.of("error","Invalid UUID")); }

      var body = JsonUtil.fromJson(req.body(), StudentUpdateRequest.class);

      List<String> errors = new ArrayList<>();
      if (body.fullName == null || body.fullName.isBlank()) errors.add("fullName is required");
      if (body.age == null || body.age < 16) errors.add("age must be >= 16");
      if (!errors.isEmpty()) { res.status(400); return JsonUtil.toJson(Map.of("errors", errors)); }

      if (hdao.findById(id).isEmpty()) { res.status(404); return JsonUtil.toJson(Map.of("error","Not found")); }

      StudentEntity updated = hdao.update(id, body.fullName, body.age);
      return JsonUtil.toJson(updated);
    });

    // Delete
    delete("/orm/students/:id", (req, res) -> {
      try {
        UUID id = UUID.fromString(req.params(":id"));
        boolean ok = hdao.deleteById(id);
        if (!ok) { res.status(404); return JsonUtil.toJson(Map.of("error","Not found")); }
        res.status(204); return "";
      } catch (IllegalArgumentException ex) {
        res.status(400); return JsonUtil.toJson(Map.of("error","Invalid UUID"));
      }
    });
  }
}
```

---

## 7) Chạy thử và kiểm tra bằng Postman

* Gọi API `/students` và `/orm/students`, kiểm tra console log cả app để xem log SQL từ hibernate
* `POST http://localhost:8080/orm/students`, log gồm 3 câu SQL sau:
  * `hdao.existsByEmail(body.email)`: Hibernate:
      select
      count(se1_0.id)
      from
      app.students se1_0
      where
      se1_0.email=?
  * `hdao.save(body.fullName, body.email, body.age)`:
    * `persist(student)`: Hibernate:
      insert
      into
      app.students
      (age, email, full_name, id)
      values
      (?, ?, ?, ?)
    * `refresh(student)`: Hibernate:
      select
      se1_0.id,
      se1_0.age,
      se1_0.created_at,
      se1_0.email,
      se1_0.full_name,
      se1_0.updated_at
      from
      app.students se1_0
      where
      se1_0.id=?
* `PUT http://localhost:8080/orm/students/{id}`, log gồm 3 câu SQL sau:
  * `hdao.update(id, body.fullName, body.age)`:
    * `session.get(StudentEntity.class, id)`: Hibernate:
      select
      se1_0.id,
      se1_0.age,
      se1_0.created_at,
      se1_0.email,
      se1_0.full_name,
      se1_0.updated_at
      from
      app.students se1_0
      where
      se1_0.id=?
    * `flush()`: hibernate nhận thấy entity `StudentEntity` đã có thay đổi giá trị, nên khi flush() sẽ sinh UPDATE SQL cho entity đó: Hibernate:
      update
      app.students
      set
      age=?,
      email=?,
      full_name=?
      where
      id=?
    * `refresh(student)`: Hibernate:
      select
      se1_0.id,
      se1_0.age,
      se1_0.created_at,
      se1_0.email,
      se1_0.full_name,
      se1_0.updated_at
      from
      app.students se1_0
      where
      se1_0.id=?