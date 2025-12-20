package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.OtpRecord;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.request.LoginRequest;
import com.oscc.skillexchange.dto.request.OtpVerificationRequest;
import com.oscc.skillexchange.dto.request.SignupRequest;
import com.oscc.skillexchange.dto.response.AuthResponse;
import com.oscc.skillexchange.exception.*;
import com.oscc.skillexchange.repository.OtpRepository;
import com.oscc.skillexchange.repository.UserRepository;
import com.oscc.skillexchange.util.AppConstants;
import com.oscc.skillexchange.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final EntityMapper mapper;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.otp.ttl-minutes:5}")
    private int otpTtlMinutes;

    @Value("${app.otp.length:6}")
    private int otpLength;

    /**
     * Initiates signup by sending OTP to email
     */
    @Transactional
    public void initiateSignup(SignupRequest request) {
        log.info("Initiating signup for email: {}", request.getEmail());

        String email = request.getEmail().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException(AppConstants.Messages.EMAIL_ALREADY_EXISTS);
        }

        generateAndSendOtp(email);
    }

    /**
     * Generates and sends OTP
     */
    @Transactional
    public void generateAndSendOtp(String email) {
        log.info("Generating OTP for email: {}", email);

        String normalizedEmail = email.toLowerCase();

        // Delete any existing OTP
        otpRepository.deleteByEmailIgnoreCase(normalizedEmail);

        // Generate new OTP
        int otpValue = generateOtpValue();
        Instant expiresAt = Instant.now().plus(otpTtlMinutes, ChronoUnit.MINUTES);

        OtpRecord otpRecord = OtpRecord.builder()
                .email(normalizedEmail)
                .otp(String.valueOf(otpValue))
                .expiresAt(expiresAt)
                .build();

        otpRepository.save(otpRecord);

        // Send email
        emailService.sendOtpEmail(normalizedEmail, otpValue);

        log.info("OTP sent successfully to: {}", normalizedEmail);
    }

    /**
     * Verifies OTP and completes signup
     */
    @Transactional
    public AuthResponse verifyOtpAndSignup(OtpVerificationRequest request) {
        log.info("Verifying OTP for email: {}", request.getEmail());

        String normalizedEmail = request.getEmail().toLowerCase();

        // Verify OTP
        OtpRecord otpRecord = otpRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new InvalidOtpException(AppConstants.Messages.OTP_INVALID));

        if (Instant.now().isAfter(otpRecord.getExpiresAt())) {
            otpRepository.delete(otpRecord);
            throw new InvalidOtpException(AppConstants.Messages.OTP_EXPIRED);
        }

        if (!otpRecord.getOtp().equals(String.valueOf(request.getOtp()))) {
            throw new InvalidOtpException(AppConstants.Messages.OTP_INVALID);
        }

        // Delete OTP after successful verification
        otpRepository.delete(otpRecord);

        // Create user
        SignupRequest signupData = request.getSignupData();
        if (signupData == null) {
            throw new BusinessException("Signup data is required");
        }

        User user = mapper.toUser(signupData);
        user.setPassword(passwordEncoder.encode(signupData.getPassword()));
        user = userRepository.save(user);

        log.info("User registered successfully: {}", user.getEmail());

        // Generate token
        String token = jwtService.generateToken(user.getEmail());
        Long expiresIn = jwtService.getExpirationTime();

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(mapper.toUserResponse(user))
                .build();
    }

    /**
     * Authenticates user and returns JWT token
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        String normalizedEmail = request.getEmail().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.isLocked()) {
            throw new AccountLockedException();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getEmail());
        Long expiresIn = jwtService.getExpirationTime();

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(mapper.toUserResponse(user))
                .build();
    }

    /**
     * Validates token and returns user
     */
    public User validateTokenAndGetUser(String token) {
        if (!jwtService.validateToken(token)) {
            throw new InvalidTokenException(AppConstants.Messages.INVALID_TOKEN);
        }

        String email = jwtService.extractEmail(token);
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.Messages.USER_NOT_FOUND));
    }

    // Helper methods
    private int generateOtpValue() {
        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;
        return min + RANDOM.nextInt(max - min + 1);
    }
}
