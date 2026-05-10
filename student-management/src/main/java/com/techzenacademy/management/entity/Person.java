package com.techzenacademy.management.entity;

import com.techzenacademy.management.constant.FieldLength;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "people")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "full_name", nullable = false, length = FieldLength.NAME_MAX_LENGTH)
    String fullName;

    LocalDate dob;

    @Column(unique = true, length = FieldLength.PHONE_MAX_LENGTH)
    String phone;

    @Column(name = "contact_email", length = FieldLength.EMAIL_MAX_LENGTH)
    String contactEmail;

    @Column(length = FieldLength.ADDRESS_MAX_LENGTH)
    String address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    Instant updatedAt;

    // Helper method for business logic
    public boolean isAdult() {
        return dob != null && dob.plusYears(18)
                .isBefore(LocalDate.now()
                        .plusDays(1));
    }
}
