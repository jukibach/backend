package com.example.identityservice.event;


import com.example.identityservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ForgotPasswordEvent {
    private User user;
    private String url;
}
