package com.oscc.skillexchange.controller;

import com.oscc.skillexchange.dto.request.LoginRequest;
import com.oscc.skillexchange.dto.request.OtpVerificationRequest;
import com.oscc.skillexchange.dto.request.SignupRequest;
import com.oscc.skillexchange.dto.response.ApiResponse;
import com.oscc.skillexchange.dto.response.AuthResponse;
import com.oscc.skillexchange.service.AuthService;
import com.oscc.skillexchange.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Initiate signup and send OTP")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists")
    @PostMapping(value = "/signup/initiate", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> initiateSignup(
            @Valid @RequestBody SignupRequest request) {
        authService.initiateSignup(request);
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.Messages.OTP_SENT, null)
        );
    }

    @Operation(summary = "Verify OTP and complete signup")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Signup successful")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @PostMapping(value = "/signup/verify", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtpAndSignup(
            @Valid @RequestBody OtpVerificationRequest request) {
        AuthResponse response = authService.verifyOtpAndSignup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.Messages.SIGNUP_SUCCESS, response));
    }

    @Operation(summary = "Login with credentials")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping(value = "/login", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.Messages.LOGIN_SUCCESS, response)
        );
    }

    @Operation(summary = "Resend OTP")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully")
    @PostMapping(value = "/otp/resend", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @RequestParam String email) {
        authService.generateAndSendOtp(email);
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.Messages.OTP_SENT, null)
        );
    }
}

