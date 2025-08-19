package com.skillexchange.service;

import com.skillexchange.model.ApiResponse;
import com.skillexchange.model.OtpDetails;
import com.skillexchange.model.UserDetails;
import com.skillexchange.model.dto.LoginDTO;
import com.skillexchange.repository.AuthRepository;
import com.skillexchange.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock AuthRepository authRepository;
    @Mock EmailService emailService;
    @Mock JwtService jwtService;
    @Mock OtpRepository otpRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AuthService authService;

    @Captor ArgumentCaptor<OtpDetails> otpDetailsCaptor;

    @BeforeEach
    void init() {
        authService = new AuthService(authRepository, emailService, jwtService, otpRepository);
        // field injection for encoder
        try {
            var f = AuthService.class.getDeclaredField("passwordEncoder");
            f.setAccessible(true);
            f.set(authService, passwordEncoder);
        } catch (Exception ignore) {}
    }

    @Test
    void generateOtp_savesToRepository_andSendsEmail() {
        ResponseEntity<?> res = authService.generateOtp("User@Example.com");

        verify(otpRepository, times(1)).save(otpDetailsCaptor.capture());
        verify(emailService, times(1)).sendEmail(eq("User@Example.com"), anyString(), contains("Your OTP"));

        OtpDetails saved = otpDetailsCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(res.getBody()).isInstanceOf(ApiResponse.class);
        ApiResponse<?> api = (ApiResponse<?>) res.getBody();
        assertThat(api.getSuccess()).isTrue();
    }

    @Test
    void verifyOtp_valid_flow_cleansUpAndSignsUp() {
        UserDetails user = UserDetails.builder()
            .email("a@b.com").name("A").phone("1").password("p")
            .skills(new String[]{"java"})
            .build();

        when(otpRepository.findTopByEmailOrderByCreatedAtDesc("a@b.com"))
            .thenReturn(Optional.of(new OtpDetails(UUID.randomUUID(), "a@b.com", 123456, Instant.now())));
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(authRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> res = authService.verifyOtp(123456, user);

        verify(otpRepository).deleteByEmail("a@b.com");
        verify(authRepository).save(any(UserDetails.class));
        ApiResponse<?> api = (ApiResponse<?>) res.getBody();
        assertThat(api.getSuccess()).isTrue();
    }

    @Test
    void verifyOtp_expired_returnsExpireMessage() {
        UserDetails user = UserDetails.builder()
            .email("a@b.com").name("A").phone("1").password("p")
            .skills(new String[]{"java"})
            .build();

        Instant old = Instant.now().minusSeconds(10 * 60);
        when(otpRepository.findTopByEmailOrderByCreatedAtDesc("a@b.com"))
            .thenReturn(Optional.of(new OtpDetails(UUID.randomUUID(), "a@b.com", 111111, old)));

        ResponseEntity<?> res = authService.verifyOtp(111111, user);
        ApiResponse<?> api = (ApiResponse<?>) res.getBody();
        assertThat(api.getSuccess()).isFalse();
        assertThat(api.getMessage()).containsIgnoringCase("expire");
        verify(otpRepository).deleteByEmail("a@b.com");
    }
}

