package com.example.identityservice.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class MessageResponse {
    private String message;
    private String status;
    private Date timeStamp;
    public MessageResponse(String message, String status) {
         this.message = message;
         this.status = status;
         this.timeStamp = new Date();
    }
}
