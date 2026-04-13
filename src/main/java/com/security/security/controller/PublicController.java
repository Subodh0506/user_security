package com.security.security.controller;

import com.security.security.dto.common.ApiResponse;
import com.security.security.dto.common.PageResponse;
import com.security.security.dto.user.CreateUserRequest;
import com.security.security.dto.user.UserResponse;
import com.security.security.service.ServiceAbs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("v1")
@RequiredArgsConstructor
public class PublicController {

    private final ServiceAbs serviceAbs;
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody CreateUserRequest userDto) {

        UserResponse userResponse = serviceAbs.save(userDto);

        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .status(HttpStatus.CREATED.value())
                .data(userResponse)
                .timestamp(LocalDateTime.now())
                .message("User created successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
