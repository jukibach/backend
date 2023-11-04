package com.example.identityservice.controller;

import com.example.identityservice.dto.AuthRequest;
import com.example.identityservice.entity.UserCredential;
import com.example.identityservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<Object> addNewUser(@RequestBody RegisterRequest request) {
        return service.saveUser(request);
    }

    @PostMapping("/token")
    public ResponseEntity<Object> getToken(@RequestBody AuthRequest authRequest) {
        /* AuthenticationManager: access to the DB -> authenticate via user detail service */
        Authentication
                authenticate
                = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if(authenticate.isAuthenticated()) {
            return service.generateToken(authenticate);
        } else {
            return new ResponseEntity<>(BAD_REQUEST);
        }
    }

    @PostMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        service.validateToken(token);
        return "Valid token";
    }
}
