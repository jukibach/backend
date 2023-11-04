package com.example.identityservice.service;

import com.example.identityservice.config.CustomUserDetails;
import com.example.identityservice.dto.JwtResponse;
import com.example.identityservice.dto.RegisterRequest;
import com.example.identityservice.dto.ShowUsersResponse;
import com.example.identityservice.dto.UserDTO;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.VerificationToken;
import com.example.identityservice.event.VerificationEvent;
import com.example.identityservice.repository.UserCredentialRepository;
import com.example.identityservice.repository.VerificationTokenRepository;
import com.example.identityservice.response.MessageResponse;
import com.example.identityservice.service.interfaces.AuthService;
import jakarta.transaction.Transactional;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class AuthServiceImp implements AuthService {
    @Autowired
    private UserCredentialRepository repository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Value("${app.client.baseUrl}")
    private String baseUrl;

    @Autowired
    private KafkaTemplate<String, VerificationEvent> kafkaTemplate;

    @Override
    @Transactional
    public ResponseEntity<Object> saveUser(RegisterRequest request) {
        if(nameExists(request.getUserName())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + request.getUserName() + " is already taken!", "INVALID"));
        }

        if(emailExists(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + request.getEmail() + " is already taken!", "INVALID"));
        }
        String token = RandomString.make(64);
        User user = new User();
        user.setBadgeId(generateBadgeId());
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEnabled(false);
        user.setCreatedDate(new Date());
        user.setUpdatedDate(new Date());
        repository.save(user);
        createVerificationToken(user, token);
        String verifyURL = baseUrl + "/email-verification?token=" + token;
        kafkaTemplate.send("verificationTopic", new VerificationEvent(user, verifyURL));
        return ResponseEntity.ok().body(new MessageResponse("User added to the system", "VALID"));
    }

    @Override
    public ResponseEntity<Object> login(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails.getUsername());
        return ResponseEntity
                .ok()
                .body(new JwtResponse(userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), token));
    }

    @Override
    @Transactional
    public ResponseEntity<Object> validateToken(String token) {
        final VerificationToken verificationToken = tokenRepository.findByToken(token);
        MessageResponse messageResponse;
        if(verificationToken == null || verificationToken.getUser().isEnabled()) {
            messageResponse
                    = new MessageResponse("Sorry, we could not verify account. It maybe already verified " + "or verification code is incorrect.", "INVALID");
            return ResponseEntity.badRequest().body(messageResponse);
        }
        User userByToken = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();

        if((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            messageResponse = new MessageResponse("Verification code was expired!", "EXPIRED");
            return ResponseEntity.badRequest().body(messageResponse);
        }
        userByToken.setEnabled(true);
        repository.save(userByToken);
        tokenRepository.delete(verificationToken);
        messageResponse = new MessageResponse("Verified successfully", "VALID");
        return ResponseEntity.ok().body(messageResponse);
    }

    @Override
    public ResponseEntity<Object> showUserWithPagination(int pageIndex, int pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort
                .by(sortBy)
                .descending();
        Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);
        List<UserDTO> userDtoList = repository.findAll(pageable).stream().map(UserDTO::new).toList();
        ShowUsersResponse response = new ShowUsersResponse();
        response.setUserList(userDtoList);
        response.setSize(pageSize);
        response.setNumber(pageIndex);
        return ResponseEntity.ok(response);
    }

    private String generateBadgeId() {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random random = new Random();
        StringBuilder sb = new StringBuilder((100000 + random.nextInt(900000)) + "-");
        for (int i = 0; i < 5; i++) {
            sb.append(chars[random.nextInt(chars.length)]);
        }
        return sb.toString();
    }

    @Transactional
    private void createVerificationToken(User user, String token) {
        VerificationToken myToken = new VerificationToken(token, user);
        tokenRepository.save(myToken);
    }

    private boolean nameExists(String name) {
        return repository.existsByUserName(name);
    }

    private boolean emailExists(String email) {
        return repository.existsByEmail(email);
    }
}
