package com.example.identityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordDTO {
    @NotBlank
    private String token;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;

}
