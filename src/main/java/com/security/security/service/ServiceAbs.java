package com.security.security.service;

import com.security.security.dto.common.PageResponse;
import com.security.security.dto.user.CreateUserRequest;
import com.security.security.dto.user.UserResponse;
import org.springframework.data.domain.Pageable;

public interface ServiceAbs {
    UserResponse save(CreateUserRequest userDto);

    PageResponse<UserResponse> getAll(Pageable pageable);

    UserResponse getByUser(String username);
}
