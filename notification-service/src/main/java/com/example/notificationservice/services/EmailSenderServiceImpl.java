package com.example.notificationservice.services;

import com.example.notificationservice.ForgotPasswordEvent;
import com.example.notificationservice.VerificationEvent;
import com.example.notificationservice.models.User;
import com.example.notificationservice.services.interfaces.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EmailSenderServiceImpl implements EmailSenderService {
    String submitContent = """
            Dear [[name]],
            <br> The order [[requestId]] status  is PENDING <br>
            <br> Please wait for confirmation<br>
            <br> Thank you! <br>""";
    @Value("${spring.mail.username}")
    private String fromAddress;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${app.client.baseUrl}")
    private String baseUrl;

//    @Override
//    public void sendNotificationEmailsWhenSubmittingRequest(RequestPlacedEvent requestPlacedEvent)
//            throws MessagingException {
//        for (Request request : requestPlacedEvent.getRequests()) {
//            sendVerificationEmail(request.getAccepter_Id(), request.getRequestId(), submitContent);
//            sendVerificationEmail(request.getNextKeeper_Id(), request.getRequestId(), submitContent);
//        }
//    }

//    private void sendVerificationEmail(int userId, String requestId, String content) throws MessagingException {
//        User user = findUserById(userId);
//        String toAddress = user.getEmail();
//        String subject = "Receive a request";
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message);
//        helper.setFrom(fromAddress);
//        helper.setTo(toAddress);
//        helper.setSubject(subject);
//        content = content.replace("[[name]]", user.getFirstName().concat(" " + user.getLastName()));
//        content = content.replace("[[requestId]]", requestId);
//        helper.setText(content, true);
//        mailSender.send(message);
//    }

    @Override
    public void sendVerificationEmail(VerificationEvent verificationEvent) throws MessagingException {
        User user = verificationEvent.getUser();
        String verifyURL = verificationEvent.getUrl();
        String toAddress = user.getEmail();
        String subject = "Please verify your registration";
        String
                content
                = "Dear [[name]],<br>" + "Please click the link below to verify your registration:<br>" + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>" + "Thank you!<br>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        content = content.replace("[[name]]", user.getFirstName().concat(" " + user.getLastName()));
        content = content.replace("[[URL]]", verifyURL);
        helper.setText(content, true);
        mailSender.send(message);
    }

    @Override
    public void sendResetPasswordEmail(ForgotPasswordEvent forgotPasswordEvent) throws MessagingException {
        User user = forgotPasswordEvent.getUser();
        String URL = forgotPasswordEvent.getUrl();
        String toAddress = user.getEmail();
        String subject = "Forgot password";
        String
                content
                = "Dear [[name]],<br>" + "Please click the link below to reset your password:<br>" + "<h3><a href=\"[[URL]]\" target=\"_self\">RESET PASSWORD</a></h3>" + "Thank you!<br>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        content = content.replace("[[name]]", user.getFirstName().concat(" " + user.getLastName()));
        content = content.replace("[[URL]]", URL);
        helper.setText(content, true);
        mailSender.send(message);
    }

    private User findUserById(int id) {
        return webClientBuilder
                .baseUrl(baseUrl)
                .build()
                .get()
                .uri("/api/users/{id}", id)
                .retrieve()
                .bodyToMono(User.class)
                .block();
    }

}
