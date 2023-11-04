package com.example.notificationservice;

import com.example.notificationservice.services.interfaces.EmailSenderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication
@EnableKafka
@Slf4j
public class NotificationServiceApplication {
	@Autowired
	EmailSenderService emailSenderService;

	@Autowired
	private ObjectMapper objectMapper;

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	@KafkaListener(topics = "verificationTopic", groupId = "verificationId", concurrency = "2")
	public void sendVerificationEmailsWhenRegistering(VerificationEvent verificationEvent)
			throws MessagingException, JsonProcessingException {
		log.info("Received verification {}", objectMapper
				.writerWithDefaultPrettyPrinter()
				.writeValueAsString(verificationEvent));
		emailSenderService.sendVerificationEmail(verificationEvent);
	}
}
