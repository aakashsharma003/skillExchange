package com.skillexchange.api;

import com.skillexchange.model.UserDetails;
import com.skillexchange.model.dto.LoginDTO;
import com.skillexchange.model.request.OtpRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/auth")
@Tag(name = "Authentication", description = "This API manages authentication flows (signup, OTP verification, login)")
public interface AuthenticationApi {
    @Operation(summary = "Sign up: request OTP")
    @ApiResponse(responseCode = "201", description = "OTP sent to the user’s contact")
    @ApiResponse(responseCode = "400", description = "Invalid signup payload")
    @ApiResponse(responseCode = "409", description = "User already exists")
    @ApiResponse(responseCode = "429", description = "Too many OTP requests")
    @PostMapping(path = "/signup-to-otp", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<?> signupToOtp(@Valid @RequestBody UserDetails signUpRequest);

    @Operation(summary = "Verify OTP for signup/login")
    @ApiResponse(responseCode = "200", description = "OTP verified successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PostMapping(path = "/verifyOtp", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpRequest otpRequest);

    @Operation(summary = "Login with credentials")
    @ApiResponse(responseCode = "200", description = "Authenticated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid login payload")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "423", description = "Account locked")
    @PostMapping(path = "/login", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginRequest);
}



