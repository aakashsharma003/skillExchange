package com.oscc.skillexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.request.LoginRequest;
import com.oscc.skillexchange.dto.request.OtpVerificationRequest;
import com.oscc.skillexchange.dto.request.SignupRequest;
import com.oscc.skillexchange.dto.response.ApiResponse;
import com.oscc.skillexchange.dto.response.AuthResponse;
import com.oscc.skillexchange.dto.response.UserResponse;
import com.oscc.skillexchange.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void testInitiateSignup_shouldReturnOkWithOtpSent() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .password("SecurePass123")
                .build();

        doNothing().when(authService).initiateSignup(any(SignupRequest.class));

        mockMvc.perform(post("/api/auth/signup/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));
    }

    @Test
    void testInitiateSignup_shouldReturnBadRequestWithInvalidEmail() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .fullName("John Doe")
                .email("invalid-email")
                .phone("+1234567890")
                .password("SecurePass123")
                .build();

        mockMvc.perform(post("/api/auth/signup/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testInitiateSignup_shouldReturnConflictWhenEmailExists() throws Exception {
        SignupRequest request = SignupRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .password("SecurePass123")
                .build();

        doThrow(new com.oscc.skillexchange.exception.DuplicateResourceException("Email already registered"))
                .when(authService).initiateSignup(any(SignupRequest.class));

        mockMvc.perform(post("/api/auth/signup/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testVerifyOtpAndSignup_shouldReturnAuthResponseOnSuccess() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .password("SecurePass123")
                .build();

        OtpVerificationRequest request = OtpVerificationRequest.builder()
                .email("john@example.com")
                .otp(123456)
                .signupData(signupRequest)
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(UserResponse.builder()
                        .id("user-1")
                        .email("john@example.com")
                        .build())
                .build();

        when(authService.verifyOtpAndSignup(any(OtpVerificationRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/signup/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void testVerifyOtpAndSignup_shouldReturnBadRequestWithInvalidOtp() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("SecurePass123")
                .build();

        OtpVerificationRequest request = OtpVerificationRequest.builder()
                .email("john@example.com")
                .otp(999999)
                .signupData(signupRequest)
                .build();

        when(authService.verifyOtpAndSignup(any())).thenThrow(
                new com.oscc.skillexchange.exception.InvalidOtpException("Invalid OTP")
        );

        mockMvc.perform(post("/api/auth/signup/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testLogin_shouldReturnAuthResponseWithValidCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("SecurePass123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(UserResponse.builder()
                        .id("user-1")
                        .email("john@example.com")
                        .build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void testLogin_shouldReturnUnauthorizedWithInvalidCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("WrongPassword")
                .build();

        when(authService.login(any())).thenThrow(
                new com.oscc.skillexchange.exception.InvalidCredentialsException()
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testResendOtp_shouldReturnOkAndSendOtp() throws Exception {
        doNothing().when(authService).generateAndSendOtp("john@example.com");

        mockMvc.perform(post("/api/auth/otp/resend")
                .param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));

        verify(authService).generateAndSendOtp("john@example.com");
    }
}
