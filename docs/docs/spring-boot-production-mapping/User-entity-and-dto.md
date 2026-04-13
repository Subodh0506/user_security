# User entity and UserDto

Place in your project: `entity/User.java`, `dto/UserDto.java`.

## User (entity)

```java
package com.example.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // avoid exposing in API
    @Column(name = "password_hash")
    private String passwordHash;
}
```

## UserDto (API response)

```java
package com.example.app.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String email;
    // no passwordHash – never expose in DTO
}
```

Use same field names as entity for the fields you want mapped; MapStruct will map them by name. Use `@Mapping(target = "field", ignore = true)` for fields that exist only on one side.
