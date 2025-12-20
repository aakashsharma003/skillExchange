package com.oscc.skillexchange.controller;

import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.request.ExchangeRequestDto;
import com.oscc.skillexchange.dto.request.UpdateExchangeRequestDto;
import com.oscc.skillexchange.dto.response.ApiResponse;
import com.oscc.skillexchange.dto.response.ExchangeRequestResponse;
import com.oscc.skillexchange.service.AuthService;
import com.oscc.skillexchange.service.ExchangeRequestService;
import com.oscc.skillexchange.util.AppConstants;
import com.oscc.skillexchange.util.TokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/api/exchange-requests")
@RequiredArgsConstructor
@Tag(name = "Exchange Requests", description = "Skill exchange request endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ExchangeRequestController {

    private final ExchangeRequestService exchangeRequestService;
    private final AuthService authService;

    @Operation(summary = "Create exchange request")
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ExchangeRequestResponse>> createRequest(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader,
            @Valid @RequestBody ExchangeRequestDto request) {
        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);
        ExchangeRequestResponse response = exchangeRequestService.createRequest(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.Messages.REQUEST_CREATED, response));
    }

    @Operation(summary = "Get all requests (sent + received)")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ExchangeRequestResponse>>> getAllRequests(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader) {
        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);
        List<ExchangeRequestResponse> requests = exchangeRequestService.getAllRequestsForUser(user.getEmail());
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @Operation(summary = "Get sent requests")
    @GetMapping(value = "/sent", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ExchangeRequestResponse>>> getSentRequests(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader) {
        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);
        List<ExchangeRequestResponse> requests = exchangeRequestService.getSentRequests(user.getId());
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @Operation(summary = "Get received requests")
    @GetMapping(value = "/received", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ExchangeRequestResponse>>> getReceivedRequests(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader) {
        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);
        List<ExchangeRequestResponse> requests = exchangeRequestService.getReceivedRequests(user.getId());
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @Operation(summary = "Update request status")
    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ExchangeRequestResponse>> updateRequest(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader,
            @PathVariable String id,
            @Valid @RequestBody UpdateExchangeRequestDto request) {
        String token = TokenUtil.extractToken(authHeader);
        authService.validateTokenAndGetUser(token);
        ExchangeRequestResponse response = exchangeRequestService.updateRequest(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.Messages.REQUEST_UPDATED, response)
        );
    }
}
