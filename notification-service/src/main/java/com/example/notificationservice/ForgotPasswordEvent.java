package com.example.notificationservice;


import com.example.notificationservice.models.User;
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
