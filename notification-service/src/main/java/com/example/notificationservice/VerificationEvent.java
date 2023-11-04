package com.example.notificationservice;

import com.example.notificationservice.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationEvent {
    private User user;
    private String url;
}
