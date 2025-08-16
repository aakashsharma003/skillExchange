package com.skillexchange.controller;

import com.skillexchange.api.UserApi;
import com.skillexchange.model.ApiResponse;
import com.skillexchange.model.UserDetails;
import com.skillexchange.model.UserDetailsUpdate;
import com.skillexchange.service.AuthService;
import com.skillexchange.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class UserController implements UserApi {

    private final AuthService authService;
    private final JwtService jwtService;

    public UserController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String tokenHeader) {
        String email = jwtService.extractEmail(tokenHeader);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new ApiResponse<>(false, "Invalid or missing token", null));
        }
        Optional<UserDetails> user = authService.findByEmailIgnoreCase(email);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new ApiResponse<>(false, "User not found", null));
        }
        UserDetails userDetailsWithoutPassword = user.get();
        userDetailsWithoutPassword.setPassword(null);
        return ResponseEntity.ok(new ApiResponse<>(true, "User found", userDetailsWithoutPassword));
    }

    @Override
    public ResponseEntity<UserDetails> userProfileUpdate(@RequestHeader("Authorization") String tokenHeader, UserDetailsUpdate updatedUser) {
        String email = jwtService.extractEmail(tokenHeader);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Optional<UserDetails> userOpt = authService.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        UserDetails user = userOpt.get();
        // Efficiently update only changed fields
        if (updatedUser.getName() != null) user.setName(updatedUser.getName());
        if (updatedUser.getSkills() != null) user.setSkills(updatedUser.getSkills());
        if (updatedUser.getBio() != null) user.setBio(updatedUser.getBio());
        // Add more fields as needed
        UserDetails updated = authService.updateUserProfile(user);
        return ResponseEntity.ok(updated);
    }

    public ResponseEntity<?> updateUser(String email, UserDetailsUpdate updatedUser) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ApiResponse<>(false, "Email is required", null));
        }
        Optional<UserDetails> userOpt = authService.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new ApiResponse<>(false, "User not found", null));
        }
        UserDetails user = userOpt.get();
        // Efficiently update only changed fields
        if (updatedUser.getName() != null) user.setName(updatedUser.getName());
        if (updatedUser.getSkills() != null) user.setSkills(updatedUser.getSkills());
        if (updatedUser.getBio() != null) user.setBio(updatedUser.getBio());
        // Add more fields as needed
        UserDetails updated = authService.updateUserProfile(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", updated));
    }

    @Override
    public ResponseEntity<?> getSomeoneProfile(String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ApiResponse<>(false, "Email is required", null));
        }
        Optional<UserDetails> user = authService.findByEmailIgnoreCase(email);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ApiResponse<>(false, "User not found for given email", null));
        }
        UserDetails userDetailsWithoutPassword = user.get();
        userDetailsWithoutPassword.setPassword(null);
        return ResponseEntity.ok(new ApiResponse<>(true, "User found", userDetailsWithoutPassword));
    }
}
