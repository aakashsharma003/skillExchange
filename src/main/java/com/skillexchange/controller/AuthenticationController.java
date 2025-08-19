package com.skillexchange.controller;

import com.skillexchange.api.AuthenticationApi;
import com.skillexchange.model.UserDetails;
import com.skillexchange.model.dto.LoginDTO;
import com.skillexchange.model.request.OtpRequest;
import com.skillexchange.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class AuthenticationController implements AuthenticationApi {

    private final AuthService authService;

    public AuthenticationController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<?> signupToOtp(@Valid UserDetails signUpRequest) {
        return authService.signupToOtp(signUpRequest);
    }

    @Override
    public ResponseEntity<?> verifyOtp(@Valid OtpRequest otpRequest) {
        return authService.verifyOtp(otpRequest.getOtp(), otpRequest.getUserDetails());
    }

    @Override
    public ResponseEntity<?> login(@Valid LoginDTO loginRequest) {
        return authService.login(loginRequest);
    }
}

