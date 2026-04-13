package com.security.security.service;

import com.security.security.dto.common.PageResponse;
import com.security.security.dto.user.UserResponse;
import com.security.security.cache.LRUCache;
import com.security.security.models.Users;
import com.security.security.repositories.UserRepo;
import com.security.security.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceImplTests {

    @Mock
    private UserRepo userRepo;

    @Mock
    private LRUCache<String, Users> localCache;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ServiceImpl service;

    @Test
    void getAll_shouldReturnPagedUserResponses_whenPageFound() {
        Pageable pageable = PageRequest.of(0, 2);
        Users user = Users.builder()
                .id(1L)
                .name("alice")
                .email("alice@example.com")
                .password("secret")
                .build();
        Page<Users> page = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepo.findAll(pageable)).thenReturn(page);

        PageResponse<UserResponse> response = service.getAll(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(response.getContent().get(0).getName()).isEqualTo("alice");
        assertThat(response.getContent().get(0).getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(2);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();

        verify(userRepo).findAll(pageable);
    }
}
