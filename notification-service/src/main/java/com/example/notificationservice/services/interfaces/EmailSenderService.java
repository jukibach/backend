package com.example.notificationservice.services.interfaces;


import com.example.notificationservice.ForgotPasswordEvent;
import com.example.notificationservice.VerificationEvent;
import jakarta.mail.MessagingException;

public interface EmailSenderService {
//    public void sendNotificationEmailsWhenSubmittingRequest(RequestPlacedEvent requestPlacedEvent) throws MessagingException;

    public void sendVerificationEmail(VerificationEvent verificationEvent) throws MessagingException;

    public void sendResetPasswordEmail(ForgotPasswordEvent forgotPasswordEvent) throws MessagingException;


}
