package com.example.identityservice.controller;

import com.example.identityservice.dto.AuthRequest;
import com.example.identityservice.dto.RegisterRequest;
import com.example.identityservice.dto.UserDTO;
import com.example.identityservice.response.MessageResponse;
import com.example.identityservice.service.interfaces.AuthService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

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

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody AuthRequest authRequest) {
        /* AuthenticationManager: access to the DB -> authenticate via user detail service */
        Authentication
                authenticate
                = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if(authenticate.isAuthenticated()) {
            return service.login(authenticate);
        } else {
            return new ResponseEntity<>(BAD_REQUEST);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Object> validateToken(@NotBlank @RequestParam("token") String token) {
        return service.validateToken(token);
    }

    @GetMapping("/users")
    public ResponseEntity<Object> showUserWithPagination(
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String sortOrder) {
        return service.showUserWithPagination(pageIndex, pageSize, sortBy, sortOrder);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable String id) {
        UserDTO user = service.getUserById(id);
        if(user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("User does not exist! ", "INVALID"));
        }
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> sendResetPasswordEmail(@NotBlank @RequestParam String email) {
        return service.sendResetPasswordEmail(email);
    }

    @PostMapping("/forgot-password-validation")
    public ResponseEntity<Object> validateForgotPasswordToken(@NotBlank @RequestParam("token") String token) {
        return service.validateToken(token);
    }
}
