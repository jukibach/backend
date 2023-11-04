package com.example.identityservice.service.interfaces;

import com.example.identityservice.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface AuthService {
    public ResponseEntity<Object> saveUser(RegisterRequest request);

    public ResponseEntity<Object> login(Authentication authentication) ;

    public ResponseEntity<Object> validateToken(String token);

    public ResponseEntity<Object> showUserWithPagination(int pageIndex, int pageSize, String sortBy, String sortOrder);

    public ResponseEntity<Object> showUserWithPagination(int pageIndex, int pageSize, String sortBy, String sortOrder);
}
