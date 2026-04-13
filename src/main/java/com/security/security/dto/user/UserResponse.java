package com.security.security.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {


    @JsonProperty
    private Long id;

    @JsonProperty("user_name")
    private String name;

    private String email;

    @JsonIgnore
    private String password;

    @JsonProperty("created_at")
    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonIgnore
    private LocalDateTime updatedAt;

    @JsonProperty("verified")
    @JsonIgnore
    private Boolean verified;

}
