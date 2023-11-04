package com.example.identityservice.service;

import com.example.identityservice.config.CustomUserDetails;
import com.example.identityservice.dto.*;
import com.example.identityservice.entity.PasswordResetToken;
import com.example.identityservice.entity.User;
import com.example.identityservice.entity.VerificationToken;
import com.example.identityservice.event.ForgotPasswordEvent;
import com.example.identityservice.event.VerificationEvent;
import com.example.identityservice.repository.PasswordResetTokenRepository;
import com.example.identityservice.repository.UserCredentialRepository;
import com.example.identityservice.repository.VerificationTokenRepository;
import com.example.identityservice.response.MessageResponse;
import com.example.identityservice.service.interfaces.AuthService;
import jakarta.transaction.Transactional;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthServiceImp implements AuthService {
    @Autowired
    private UserCredentialRepository repository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Value("${app.client.baseUrl}")
    private String baseUrl;

    @Autowired
    private KafkaTemplate<String, VerificationEvent> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, ForgotPasswordEvent> forgotPasswordEventKafkaTemplate;

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
    @Transactional
    public ResponseEntity<Object> validateForgotPasswordToken(String token) {
        final Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);
        MessageResponse messageResponse;
        if(passwordResetToken.isEmpty()) {
            messageResponse
                    = new MessageResponse("Sorry, we could not verify account. It maybe already verified " + "or verification code is incorrect.", "INVALID");
            return ResponseEntity.badRequest().body(messageResponse);
        }
        Calendar cal = Calendar.getInstance();
        boolean isTokenExpired = (passwordResetToken.get().getExpiryDate().getTime() - cal.getTime().getTime()) <= 0;
        if(isTokenExpired) {
            messageResponse = new MessageResponse("Verification code was expired!", "EXPIRED");
            return ResponseEntity.badRequest().body(messageResponse);
        }
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

    @Override
    @Cacheable(value = "user", key = "#id", unless = "#result == null")
    public UserDTO getUserById(String id) {
        Optional<User> user = repository.findById(id);
        return user.map(UserDTO::new).orElse(null);
    }

    @Override
    public ResponseEntity<Object> resendVerificationToken(String email) {
        VerificationToken token = generateNewToken(email);
        if(token == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Token does not exist", "INVALID"));
        }
        User user = token.getUser();
        String verifyURL = baseUrl + "/email-verification?token=" + token.getToken();
        kafkaTemplate.send("verificationTopic", new VerificationEvent(user, verifyURL));
        return ResponseEntity.ok().body(new MessageResponse("Resent successfully", "VALID"));
    }

    @Override
    public ResponseEntity<Object> sendResetPasswordEmail(String email) {
        String token = RandomString.make(64);
        Optional<User> user = repository.findByEmail(email);
        String verifyURL;
        if(user.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User does not exist", "INVALID"));
        }

        Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByUserId(user.get().getId());
        if(existingToken.isPresent()) {
            String newToken = generateResetPasswordToken(existingToken.get().getToken());
            verifyURL = baseUrl + "/forgot-password?token=" + newToken;
        } else {
            PasswordResetToken myToken = new PasswordResetToken(token, user.get());
            passwordResetTokenRepository.save(myToken);
            verifyURL = baseUrl + "/forgot-password?token=" + token;
        }
        forgotPasswordEventKafkaTemplate.send("forgotPasswordTopic", new ForgotPasswordEvent(user.get(), verifyURL));
        return ResponseEntity.ok(new MessageResponse("Sent successfully", "VALID"));

    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveForgotPassword(ForgotPasswordDTO request) {
        Optional<User> user = Optional.ofNullable(findUserByForgotPasswordToken(request.getToken()));
        MessageResponse messageResponse;
        if(user.isEmpty()) {
            messageResponse = new MessageResponse("User does not exist", "INVALID");
            return ResponseEntity.badRequest().body(messageResponse);
        }

        Optional<PasswordResetToken> token = passwordResetTokenRepository.findByToken(request.getToken());
        if(token.isEmpty()) {
            messageResponse = new MessageResponse("Token does not exist", "INVALID");
            return ResponseEntity.badRequest().body(messageResponse);
        }

        boolean arePasswordsEqual = Objects.equals(request.getNewPassword(), request.getConfirmPassword());
        if(!arePasswordsEqual) {
            messageResponse = new MessageResponse("New password and confirm password are not equal!", "INVALID");
            return ResponseEntity.badRequest().body(messageResponse);
        }

        user.get().setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user.get());
        passwordResetTokenRepository.delete(token.get());
        messageResponse = new MessageResponse("Saved successfully", "VALID");
        return ResponseEntity.ok().body(messageResponse);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveResetPassword(ResetPasswordDTO request) {
        Optional<User> user = repository.findById(request.getUserId());
        MessageResponse messageResponse;
        if(user.isEmpty()) {
            messageResponse = new MessageResponse("User does not exist", "INVALID");
            return ResponseEntity.badRequest().body(messageResponse);
        }


        boolean isOldPasswordCorrect = BCrypt.checkpw(request.getOldPassword(), user.get().getPassword());
        if(!isOldPasswordCorrect) {
            messageResponse = new MessageResponse("Old password is incorrect", "EXPIRED");
            return ResponseEntity.badRequest().body(messageResponse);
        }

        boolean arePasswordsEqual = Objects.equals(request.getNewPassword(), request.getConfirmPassword());
        if(!arePasswordsEqual) {
            messageResponse = new MessageResponse("New password and confirm password are not equal!", "INVALID");
            return ResponseEntity.badRequest().body(messageResponse);
        }

        user.get().setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user.get());
        messageResponse = new MessageResponse("Saved successfully", "VALID");
        return ResponseEntity.ok().body(messageResponse);
    }

    private User findUserByForgotPasswordToken(String existingToken) {
        Optional<PasswordResetToken> token = passwordResetTokenRepository.findByToken(existingToken);
        return token.map(PasswordResetToken::getUser).orElse(null);
    }

    @Transactional
    private String generateResetPasswordToken(String existingToken) {
        Optional<PasswordResetToken> token = passwordResetTokenRepository.findByToken(existingToken);
        if(token.isEmpty()) {
            return null;
        }
        token.get().updateToken(RandomString.make(64));
        token = Optional.of(passwordResetTokenRepository.save(token.get()));
        return token.get().getToken();
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
    private VerificationToken generateNewToken(String email) {
        Optional<User> user = repository.findByEmail(email);
        if(user.isEmpty()) {
            return null;
        }
        Optional<VerificationToken> token = tokenRepository.findByUserId(user.get().getId());
        if(token.isEmpty()) {
            return null;
        }
        token.get().updateToken(RandomString.make(64));
        tokenRepository.save(token.get());
        return token.get();
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
