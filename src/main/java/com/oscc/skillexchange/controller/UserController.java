package com.oscc.skillexchange.controller;

import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.response.ApiResponse;
import com.oscc.skillexchange.dto.response.UserResponse;
import com.oscc.skillexchange.service.AuthService;
import com.oscc.skillexchange.service.UserService;
import com.oscc.skillexchange.util.AppConstants;
import com.oscc.skillexchange.util.TokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Get current user profile")
    @GetMapping(value = "/profile", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader) {
        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);
        User userEntity = userService.getUserById(user.getId());
        UserResponse response = userService.getUserResponse(userEntity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update user profile")
    @PutMapping(value = "/profile", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader,
            @Valid @RequestBody ApiResponse.UpdateProfileRequest request) {
        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);
        UserResponse response = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.Messages.PROFILE_UPDATED, response)
        );
    }

    @Operation(summary = "Get user by email")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
            @RequestParam String email) {
        User user = userService.getUserByEmail(email);
        UserResponse response = userService.getUserResponse(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Search users by skillsOffered or interests")
    @GetMapping(value = "/search", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String interest,
            @RequestParam(required = false, defaultValue = "skillsOffered") String searchType) {
        String token = TokenUtil.extractToken(authHeader);
        User currentUser = authService.validateTokenAndGetUser(token);

        List<UserResponse> users;
        if (skill != null && !skill.isBlank()) {
            users = userService.searchUsersBySkill(skill, currentUser.getEmail());
        } else if (interest != null && !interest.isBlank()) {
            users = userService.searchUsersByInterest(interest, currentUser.getEmail());
        } else {
            users = userService.getAllUsers(currentUser.getEmail());
        }

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "Get all distinct skills")
    @GetMapping(value = "/skills", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<String>>> getAllSkills() {
        List<String> skills = userService.getAllDistinctSkills();
        return ResponseEntity.ok(ApiResponse.success(skills));
    }

    @Operation(summary = "Delete user account")
    @DeleteMapping(value = "/delete-account", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> deleteAccount(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader) {
        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);
        userService.deleteUserAccount(user.getId());
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.Messages.ACCOUNT_DELETED)
        );
    }

    @Operation(summary = "Report suspicious activity")
    @PostMapping(value = "/report-activity", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> reportSuspiciousActivity(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader,
            @RequestBody Map<String, String> request) {
        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);
        
        String description = request.get("description");
        String relatedUser = request.get("relatedUser");
        
        userService.reportSuspiciousActivity(user.getId(), description, relatedUser);
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.Messages.REPORT_SUBMITTED)
        );
    }
}
