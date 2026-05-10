package com.techzenacademy.management.entity;

import com.techzenacademy.management.constant.FieldLength;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "students")
public class Student {
    @Id
    @Column(name = "person_id")
    UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id")
    Person person;

    @Column(name = "student_code", unique = true, nullable = false, length = FieldLength.STUDENT_CODE_MAX_LENGTH)
    String studentCode;

    @Column(name = "enrollment_year")
    Integer enrollmentYear;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    Instant updatedAt;
}
