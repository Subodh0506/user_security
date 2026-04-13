package com.security.security.repositories;

import com.security.security.dto.user.CreateUserRequest;

import com.security.security.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<Users, Long> {
    Users findByName(String name);
}
