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
import com.oscc.skillexchange.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private User testUser;
    private OtpRecord testOtpRecord;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        signupRequest = SignupRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .password("SecurePassword123")
                .skillsOffered(List.of("Java"))
                .interests(List.of("Python"))
                .build();

        testUser = User.builder()
                .id("user-1")
                .fullName("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .enabled(true)
                .locked(false)
                .build();

        testOtpRecord = OtpRecord.builder()
                .id("otp-1")
                .email("john@example.com")
                .otp("123456")
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .build();
    }

    @Test
    void testInitiateSignup_shouldSendOtpWhenEmailNotExists() {
        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
        doNothing().when(emailService).sendOtpEmail(anyString(), anyInt());
        doNothing().when(otpRepository).deleteByEmailIgnoreCase(anyString());
        when(otpRepository.save(any(OtpRecord.class))).thenReturn(testOtpRecord);

        authService.initiateSignup(signupRequest);

        verify(userRepository).existsByEmailIgnoreCase("john@example.com");
        verify(emailService).sendOtpEmail(eq("john@example.com"), anyInt());
    }

    @Test
    void testInitiateSignup_shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.initiateSignup(signupRequest));
        verify(userRepository).existsByEmailIgnoreCase("john@example.com");
    }

    @Test
    void testGenerateAndSendOtp_shouldCreateAndSendOtp() {
        doNothing().when(otpRepository).deleteByEmailIgnoreCase("john@example.com");
        when(otpRepository.save(any(OtpRecord.class))).thenReturn(testOtpRecord);
        doNothing().when(emailService).sendOtpEmail(anyString(), anyInt());

        authService.generateAndSendOtp("john@example.com");

        ArgumentCaptor<OtpRecord> captor = ArgumentCaptor.forClass(OtpRecord.class);
        verify(otpRepository).save(captor.capture());
        assertEquals("john@example.com", captor.getValue().getEmail());
        assertNotNull(captor.getValue().getOtp());
        verify(emailService).sendOtpEmail(eq("john@example.com"), anyInt());
    }

    @Test
    void testVerifyOtpAndSignup_shouldCreateUserWithValidOtp() {
        OtpVerificationRequest otpRequest = OtpVerificationRequest.builder()
                .email("john@example.com")
                .otp(123456)
                .signupData(signupRequest)
                .build();

        when(otpRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(testOtpRecord));
        when(mapper.toUser(signupRequest)).thenReturn(testUser);
        when(passwordEncoder.encode("SecurePassword123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken("john@example.com")).thenReturn("token123");
        when(jwtService.getExpirationTime()).thenReturn(3600L);
        when(mapper.toUserResponse(testUser)).thenReturn(
                com.oscc.skillexchange.dto.response.UserResponse.builder()
                        .id("user-1")
                        .email("john@example.com")
                        .build()
        );

        AuthResponse response = authService.verifyOtpAndSignup(otpRequest);

        assertNotNull(response);
        assertEquals("token123", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        verify(userRepository).save(any(User.class));
        verify(otpRepository).delete(testOtpRecord);
    }

    @Test
    void testVerifyOtpAndSignup_shouldThrowExceptionWithExpiredOtp() {
        OtpRecord expiredOtp = OtpRecord.builder()
                .email("john@example.com")
                .otp("123456")
                .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .build();

        OtpVerificationRequest otpRequest = OtpVerificationRequest.builder()
                .email("john@example.com")
                .otp(123456)
                .signupData(signupRequest)
                .build();

        when(otpRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(expiredOtp));

        assertThrows(InvalidOtpException.class, () -> authService.verifyOtpAndSignup(otpRequest));
    }

    @Test
    void testVerifyOtpAndSignup_shouldThrowExceptionWithInvalidOtp() {
        OtpVerificationRequest otpRequest = OtpVerificationRequest.builder()
                .email("john@example.com")
                .otp(999999)
                .signupData(signupRequest)
                .build();

        when(otpRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(testOtpRecord));

        assertThrows(InvalidOtpException.class, () -> authService.verifyOtpAndSignup(otpRequest));
    }

    @Test
    void testLogin_shouldReturnTokenWithValidCredentials() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("SecurePassword123")
                .build();

        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("SecurePassword123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("john@example.com")).thenReturn("token123");
        when(jwtService.getExpirationTime()).thenReturn(3600L);
        when(mapper.toUserResponse(testUser)).thenReturn(
                com.oscc.skillexchange.dto.response.UserResponse.builder()
                        .id("user-1")
                        .email("john@example.com")
                        .build()
        );

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("token123", response.getToken());
        verify(userRepository).findByEmailIgnoreCase("john@example.com");
    }

    @Test
    void testLogin_shouldThrowExceptionWithInvalidEmail() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password")
                .build();

        when(userRepository.findByEmailIgnoreCase("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_shouldThrowExceptionWithInvalidPassword() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("WrongPassword")
                .build();

        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_shouldThrowExceptionWhenAccountLocked() {
        User lockedUser = User.builder()
                .id("user-1")
                .email("john@example.com")
                .password("encodedPassword")
                .locked(true)
                .build();

        LoginRequest loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("SecurePassword123")
                .build();

        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(lockedUser));

        assertThrows(AccountLockedException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testValidateTokenAndGetUser_shouldReturnUserWithValidToken() {
        when(jwtService.validateToken("validToken")).thenReturn(true);
        when(jwtService.extractEmail("validToken")).thenReturn("john@example.com");
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(testUser));

        User result = authService.validateTokenAndGetUser("validToken");

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void testValidateTokenAndGetUser_shouldThrowExceptionWithInvalidToken() {
        when(jwtService.validateToken("invalidToken")).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> authService.validateTokenAndGetUser("invalidToken"));
    }

    @Test
    void testValidateTokenAndGetUser_shouldThrowExceptionWhenUserNotFound() {
        when(jwtService.validateToken("validToken")).thenReturn(true);
        when(jwtService.extractEmail("validToken")).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmailIgnoreCase("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.validateTokenAndGetUser("validToken"));
    }
}
