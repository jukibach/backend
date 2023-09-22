package com.example.identityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private int id;
    private String username;
    private String email;

//    private String isEnable;

    private String accessToken;

//    private List<String> roles;
}
