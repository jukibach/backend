package com.example.identityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDTO {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String userId;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;

}
