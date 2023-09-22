package com.example.apigateway.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UnauthorizedRequest.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    private Message handleMessageAuth(UnauthorizedRequest e, HttpServletRequest request) {
        Message message = new Message();
        message.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        message.setStatus(HttpStatus.UNAUTHORIZED.value());
        message.setMessage(e.getMessage());
        message.setPath(String.valueOf(request.getRequestURI()));
        return message;
    }
}
