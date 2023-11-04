package com.example.identityservice.repository;

import com.example.identityservice.entity.PasswordResetToken;
import com.example.identityservice.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    public Optional<PasswordResetToken> findByUserId(String id);

    public Optional<PasswordResetToken> findByToken(String token);

}
