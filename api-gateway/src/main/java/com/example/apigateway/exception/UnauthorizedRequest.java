package com.example.apigateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedRequest extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1;

    public UnauthorizedRequest(String message){
        super(message);
    }

}
