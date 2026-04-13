package com.security.security.controller;

import com.security.security.dto.common.ApiResponse;
import com.security.security.dto.common.PageResponse;
import com.security.security.dto.user.UserResponse;
import com.security.security.service.ServiceAbs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("v1")
@RequiredArgsConstructor
public class UserController {

    private final ServiceAbs serviceAbs;

    @GetMapping(value = "/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        PageResponse<UserResponse> result = serviceAbs.getAll(pageable);
        ApiResponse<PageResponse<UserResponse>> response = ApiResponse.<PageResponse<UserResponse>>builder()
                .status(HttpStatus.OK.value())
                .data(result)
                .timestamp(LocalDateTime.now())
                .message("Users fetched successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable String username) {
        UserResponse userResponse = serviceAbs.getByUser(username);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .data(userResponse)
                .timestamp(LocalDateTime.now())
                .message("User fetched successfully")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
