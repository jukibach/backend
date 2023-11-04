package com.example.identityservice.service.interfaces;

import com.example.identityservice.dto.ForgotPasswordDTO;
import com.example.identityservice.dto.RegisterRequest;
import com.example.identityservice.dto.UserDTO;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface AuthService {
    public ResponseEntity<Object> saveUser(RegisterRequest request);

    public ResponseEntity<Object> login(Authentication authentication);

    public UserDTO getUserById(String id);

    public ResponseEntity<Object> validateToken(String token);

    public ResponseEntity<Object> resendVerificationToken(String email);


    @Transactional
    ResponseEntity<Object> validateForgotPasswordToken(String token);

    public ResponseEntity<Object> showUserWithPagination(int pageIndex, int pageSize, String sortBy, String sortOrder);

    ResponseEntity<Object> sendResetPasswordEmail(String email);

    @Transactional
    ResponseEntity<Object> saveForgotPassword(ForgotPasswordDTO request);
}
