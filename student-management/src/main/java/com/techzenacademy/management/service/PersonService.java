package com.techzenacademy.management.service;

import com.techzenacademy.management.dto.person.PersonCreateRequest;
import com.techzenacademy.management.dto.person.PersonDetailResponse;
import com.techzenacademy.management.dto.person.PersonListItemResponse;
import com.techzenacademy.management.entity.Person;
import com.techzenacademy.management.mapper.PersonMapper;
import com.techzenacademy.management.repository.PersonRepository;
import com.techzenacademy.management.util.NormalizerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    /**
     * Lấy danh sách tóm tắt tất cả mọi người
     */
    public List<PersonListItemResponse> getAllPeople() {
        return personRepository.findAll().stream()
                .map(personMapper::toListItemResponse)
                .toList();
    }

    /**
     * Lấy chi tiết thông tin một người theo ID
     */
    public PersonDetailResponse getPersonById(UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người với ID: " + id));
        return personMapper.toDetailResponse(person);
    }

    /**
     * Tạo mới một người
     */
    public PersonDetailResponse createPerson(PersonCreateRequest req) {
        String email = NormalizerUtil.normalizeEmail(req.getContactEmail());
        String phone = NormalizerUtil.normalizePhone(req.getPhone());

        if (email != null && personRepository.existsByContactEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email liên hệ đã tồn tại");
        }
        if (phone != null && personRepository.existsByPhone(phone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số điện thoại đã tồn tại");
        }

        Person person = Person.builder()
                .fullName(req.getFullName())
                .dob(req.getDob())
                .phone(phone)
                .contactEmail(email)
                .address(req.getAddress())
                .build();

        person = personRepository.save(person);
        return personMapper.toDetailResponse(person);
    }

    /**
     * Xóa thông tin một người theo ID
     */
    public void deletePerson(UUID id) {
        if (!personRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người với ID: " + id);
        }
        personRepository.deleteById(id);
    }
}
