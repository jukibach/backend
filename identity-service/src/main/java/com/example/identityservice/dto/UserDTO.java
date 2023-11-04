package com.example.identityservice.dto;

import com.example.identityservice.entity.User;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserDTO implements Serializable {
    private String id;

    private String badgeId;

    private String userName;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    public UserDTO(User user) {
        this.id = user.getId();
        this.badgeId = user.getBadgeId();
        this.userName = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
    }
}
