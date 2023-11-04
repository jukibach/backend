package com.example.identityservice.repository;

import com.example.identityservice.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    public VerificationToken findByToken(String token);

    public Optional<VerificationToken> findByUserId(String id);

}
