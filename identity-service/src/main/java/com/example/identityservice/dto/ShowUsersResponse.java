package com.example.identityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowUsersResponse {
    private List<UserDTO> userList;
    private int size;
    private int number;
}
