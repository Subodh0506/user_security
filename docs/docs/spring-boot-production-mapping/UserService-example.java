// Production service using the mapper. Place in: com.example.app.service.UserService

package com.example.app.service;

import com.example.app.dto.UserDto;
import com.example.app.entity.User;
import com.example.app.mapper.UserMapper;
import com.example.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        List<User> entities = userRepository.findAll();
        return userMapper.toDtoList(entities);
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElse(null);  // or throw UserNotFoundException
    }
}
