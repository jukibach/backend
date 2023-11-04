package com.example.identityservice.dto;

import com.example.identityservice.validation.annotations.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String userName;


    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    private String matchingPassword;

    @NotBlank
    @Size(max = 40)
    private String firstName;

    @NotBlank
    @Size(max = 40)
    private String lastName;

    @NotBlank
    @Size(max = 50)
    @ValidEmail
    private String email;

    @NotBlank
    @Size(min = 10, max = 12)
    private String phoneNumber;
}
