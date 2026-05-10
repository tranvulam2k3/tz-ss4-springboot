package com.techzenacademy.management.repository;

import com.techzenacademy.management.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {

    // Tìm người theo tên (tìm gần đúng và không phân biệt hoa thường)
    List<Person> findByFullNameContainingIgnoreCase(String keyword);

    // Tìm người theo số điện thoại
    Optional<Person> findByPhone(String phone);

    // Kiểm tra số điện thoại có tồn tại
    boolean existsByPhone(String phone);

    // Tìm người theo email liên hệ (không phân biệt hoa thường)
    List<Person> findByContactEmailIgnoreCase(String email);

    // Kiểm tra email liên hệ có tồn tại (không phân biệt hoa thường)
    boolean existsByContactEmailIgnoreCase(String email);
}
