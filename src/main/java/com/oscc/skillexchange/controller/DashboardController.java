package com.oscc.skillexchange.controller;

import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.response.ApiResponse;
import com.oscc.skillexchange.dto.response.DashboardStatsResponse;
import com.oscc.skillexchange.service.AuthService;
import com.oscc.skillexchange.service.DashboardService;
import com.oscc.skillexchange.util.AppConstants;
import com.oscc.skillexchange.util.TokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard statistics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    @Operation(summary = "Get dashboard statistics")
    @GetMapping(value = "/stats", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader) {
        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);
        
        DashboardStatsResponse stats = dashboardService.getDashboardStats(user.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
