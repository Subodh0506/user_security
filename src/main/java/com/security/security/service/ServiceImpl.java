package com.security.security.service;


import com.security.security.cache.LRUCache;

import com.security.security.dto.common.PageResponse;
import com.security.security.dto.user.CreateUserRequest;
import com.security.security.dto.user.UserResponse;
import com.security.security.exception.UserFoundException;
import com.security.security.exception.UserNotFoundException;
import com.security.security.models.Users;
import com.security.security.repositories.UserRepo;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Slf4j
public class ServiceImpl implements ServiceAbs {

    private UserRepo userRepo;
    private LRUCache<String, Users> localCache;
    private EmailService emailService;

    @Override
    public UserResponse save(CreateUserRequest createRequest) {
        Users users = Users.builder()
                .name(createRequest.getName())
                .email(createRequest.getEmail())
                .password(createRequest.getPassword()).build();

        try {
            users = userRepo.save(users);
            emailService.sendHtmlEmail(users.getEmail(), users.getName());
            log.info("saved details to db");
            localCache.put(users.getName(), users);
            log.info("added in cache");
            users = userRepo.findById(users.getId()).orElseThrow(() -> new UserNotFoundException("user not found"));
            return UserResponse.builder()
                    .id(users.getId())
                    .name(users.getName())
                    .email(users.getEmail())
                    .password(users.getPassword()).build();
        } catch (DataIntegrityViolationException e) {
            throw new UserFoundException("username already exists");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<UserResponse> getAll(Pageable pageable) {
        Page<Users> page = userRepo.findAll(pageable);
        List<UserResponse> content = page.getContent().stream()
                .map(this::toDo)
                .collect(Collectors.toList());
        log.info("fetched page {} with {} items", page.getNumber(), content.size());
        return PageResponse.<UserResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponse getByUser(String username) {
        Users users = localCache.get(username);
        if (users != null) {
            log.info("return data from cache");
        } else {
            users = userRepo.findByName(username);
            if (users == null) {
                throw new UserNotFoundException("User not found: " + username);
            }
            log.info("return from db");
            localCache.put(users.getName(), users);
            log.info("cache updated with {}", users);
        }
        return UserResponse.builder()
                .id(users.getId())
                .name(users.getName())
                .email(users.getEmail())
                .createdAt(users.getCreatedAt())
                .updatedAt(users.getUpdatedAt())
                .build();
    }

    private UserResponse toDo(Users users) {
        return  UserResponse.builder()
                .id(users.getId())
                .name(users.getName())
                .email(users.getEmail())
                .createdAt(users.getCreatedAt())
                .updatedAt(users.getUpdatedAt())
                .build();
    }


}

