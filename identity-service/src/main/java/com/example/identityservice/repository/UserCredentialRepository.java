package com.example.identityservice.repository;

import com.example.identityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserName(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUserName(String userName);

    Boolean existsByEmail(String email);

    Optional<User> findById(String id);
}