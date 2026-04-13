// Production-level mapper: MapStruct, null-safe, Spring bean.
// Place in: com.example.app.mapper.UserMapper

package com.example.app.mapper;

import com.example.app.dto.UserDto;
import com.example.app.entity.User;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper: Entity <-> DTO. Generated implementation at compile time (no reflection).
 * componentModel = "spring" -> @Component, injectable.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)  // never map sensitive fields to DTO
    UserDto toDto(User entity);

    /**
     * Null-safe list mapping. Prefer this over streaming in service for consistency.
     */
    List<UserDto> toDtoList(List<User> entities);

    /**
     * Optional: for PATCH/update flows. Only non-null DTO fields overwrite entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserDto dto, @MappingTarget User entity);
}
