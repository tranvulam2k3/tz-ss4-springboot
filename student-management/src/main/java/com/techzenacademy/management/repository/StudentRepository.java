package com.techzenacademy.management.repository;

import com.techzenacademy.management.entity.Student;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class StudentRepository {
    private final Map<UUID, Student> db = new HashMap<>();

    public Student save(Student s) {
        if (s.getId() == null)
            s.setId(UUID.randomUUID());
        db.put(s.getId(), s);
        return s;
    }
}
