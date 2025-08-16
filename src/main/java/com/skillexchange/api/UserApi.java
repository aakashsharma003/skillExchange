package com.skillexchange.api;

import com.skillexchange.model.UserDetails;
import com.skillexchange.model.UserDetailsUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/user", produces = APPLICATION_JSON_VALUE)
@Tag(name = "User", description = "User profile APIs")
public interface UserApi {

    @Operation(summary = "Get authenticated user's profile")
    @ApiResponse(responseCode = "200", description = "Profile fetched successfully")
    @ApiResponse(responseCode = "400", description = "Missing or invalid Authorization header")
    @ApiResponse(responseCode = "401", description = "Invalid/expired token")
    @GetMapping("/profile")
    ResponseEntity<?> getProfile(@RequestHeader("Authorization") String tokenHeader);

    @Operation(summary = "Update authenticated user's profile")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "400", description = "Missing or invalid Authorization header")
    @ApiResponse(responseCode = "401", description = "Invalid/expired token")
    @PutMapping(path = "/update", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<UserDetails> userProfileUpdate(
        @RequestHeader("Authorization") String tokenHeader,
        @Valid @RequestBody UserDetailsUpdate updatedUser
    );

    @Operation(summary = "Get someone else's public profile by email")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "400", description = "Bad email or user not found")
    @GetMapping("/get-someone-profile")
    ResponseEntity<?> getSomeoneProfile(
        @RequestParam
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email
    );
}
