package com.security.security.dto.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.security.security.validation.annotation.ValidPassword;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ValidPassword
@Builder
public class CreateUserRequest {

    @JsonProperty("user_id")
    @Null(message = "user_id not required")
    private Long id;

    @JsonProperty("user_name")
    @NotBlank(message = "username cannot be empty")
    @Size(min = 3, message = "username must be at least 3 characters")
    private String name;


    @JsonProperty("email")
    @NotBlank(message = "email should not be blank")
    @Email(message = "invalid mail format")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]{3,}@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "email must have at least 3 characters before @ and contain a valid domain"
    )
    private String email;


    @NotBlank(message = "password cannot be empty")
    @Size(min = 8, message = "password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "password must contain uppercase, lowercase, number and special character"
    )
    private String password;

}

