package com.techzenacademy.management.mapper;

import com.techzenacademy.management.dto.person.PersonDetailResponse;
import com.techzenacademy.management.dto.person.PersonListItemResponse;
import com.techzenacademy.management.entity.Person;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {
    /**
     * Chuyển đổi từ Person entity sang PersonListItemResponse
     */
    public PersonListItemResponse toListItemResponse(Person p) {
        if (p == null)
            return null;
        return new PersonListItemResponse(
                p.getId(),
                p.getFullName(),
                p.getPhone());
    }

    /**
     * Chuyển đổi từ Person entity sang PersonDetailResponse
     */
    public PersonDetailResponse toDetailResponse(Person p) {
        if (p == null)
            return null;
        return new PersonDetailResponse(
                p.getId(),
                p.getFullName(),
                p.getDob(),
                p.getPhone(),
                p.getContactEmail(),
                p.getAddress());
    }
}
