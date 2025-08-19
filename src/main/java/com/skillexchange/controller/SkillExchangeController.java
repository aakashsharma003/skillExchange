package com.skillexchange.controller;

import com.skillexchange.model.ApiResponse;
import com.skillexchange.model.UserDetails;
import com.skillexchange.model.dto.ExchangeRequestCardDTO;
import com.skillexchange.model.request.ExchangeRequest;
import com.skillexchange.service.AuthService;
import com.skillexchange.service.ExchangeRequestsService;
import com.skillexchange.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.skillexchange.api.SkillExchangeApi;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@org.springframework.web.bind.annotation.RequestMapping("/skill-swap")
public class SkillExchangeController implements SkillExchangeApi {

    private final AuthService authService;
    private final ExchangeRequestsService exchangeRequestsService;
    private final JwtService jwtService;

    public SkillExchangeController(
        AuthService authService,
        ExchangeRequestsService exchangeRequestsService,
        JwtService jwtService
    ) {
        this.authService = authService;
        this.exchangeRequestsService = exchangeRequestsService;
        this.jwtService = jwtService;
    }

    public ResponseEntity<?> swapRequest(String tokenHeader, ExchangeRequest exchangeRequest) {
        try {
            if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(new ApiResponse<>(false, "Missing token", null));
            }
            String token = tokenHeader.substring(7);
            if (!jwtService.validateTokenWithEmail(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .body(new ApiResponse<>(false, "Invalid token", null));
            }
            String senderEmail = jwtService.extractEmail(token);
            Optional<UserDetails> senderOpt = authService.findByEmailIgnoreCase(senderEmail);
            if (senderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .body(new ApiResponse<>(false, "User not found", null));
            }
            exchangeRequest.setSenderId(senderOpt.get().getId());
            Optional<ExchangeRequest> existingRequest = exchangeRequestsService.findBySenderReceiverAndSkill(
                exchangeRequest.getSenderId(),
                exchangeRequest.getReceiverId(),
                exchangeRequest.getRequestedSkill()
            );
            if (existingRequest.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(false,
                                                           "Duplicate request: You already sent a request for this skill.", null));
            }
            if (exchangeRequest.getStatus() == null) exchangeRequest.setStatus("pending");
            if (exchangeRequest.getCreatedAt() == null) exchangeRequest.setCreatedAt(LocalDateTime.now());
            if (exchangeRequest.getOfferedSkill() == null) exchangeRequest.setOfferedSkill("");
            exchangeRequest.setUpdatedAt(LocalDateTime.now());
            exchangeRequestsService.saveExchangeRequest(exchangeRequest);
            return ResponseEntity.ok(new ApiResponse<>(true, "Swap request saved successfully!", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse<>(false, "Failed to save swap request", e.getMessage()));
        }
    }

    public ResponseEntity<?> getAllRequest(String tokenHeader) {
        try {
            String email = jwtService.extractEmail(tokenHeader);
            Optional<UserDetails> userOpt = authService.findByEmailIgnoreCase(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .body(new ApiResponse<>(false, "User not found", null));
            }
            UserDetails user = userOpt.get();
            UUID userId = user.getId();
            List<ExchangeRequestCardDTO> response = new ArrayList<>();
            List<ExchangeRequest> receivedRequests = exchangeRequestsService.findAllReceiver(userId);
            List<ExchangeRequest> sentRequests = exchangeRequestsService.findAllSender(userId);
            List<ExchangeRequest> allRequests = new ArrayList<>();
            allRequests.addAll(receivedRequests);
            allRequests.addAll(sentRequests);
            for (var request : allRequests) {
                Optional<UserDetails> sender = authService.findByUserId(request.getSenderId());
                Optional<UserDetails> receiver = authService.findByUserId(request.getReceiverId());
                if (sender.isPresent() && receiver.isPresent()) {
                    response.add(new ExchangeRequestCardDTO(request, sender.get(), receiver.get()));
                }
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Successfully fetched requests", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse<>(false, "Failed to fetch requests", e.getMessage()));
        }
    }

    public ResponseEntity<?> getAllSentRequest(String tokenHeader) {
        try {
            String email = jwtService.extractEmail(tokenHeader);
            Optional<UserDetails> userDetailsOpt = authService.findByEmailIgnoreCase(email);
            if (userDetailsOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(new ApiResponse<>(false, "User not found", null));
            }
            UserDetails userDetails = userDetailsOpt.get();
            List<ExchangeRequestCardDTO> response = new ArrayList<>();
            List<ExchangeRequest> sentRequests = exchangeRequestsService.findAllSender(userDetails.getId());
            for (var request : sentRequests) {
                Optional<UserDetails> sender = authService.findByUserId(request.getSenderId());
                Optional<UserDetails> receiver = authService.findByUserId(request.getReceiverId());
                if (sender.isPresent() && receiver.isPresent()) {
                    response.add(new ExchangeRequestCardDTO(request, sender.get(), receiver.get()));
                }
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Successfully fetched all sent requests", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse<>(false, "Failed to fetch sent requests", e.getMessage()));
        }
    }

    // Alias to match client route: /skill-swap/sent-request
    @org.springframework.web.bind.annotation.GetMapping("/sent-request")
    public ResponseEntity<?> aliasSentRequests(@org.springframework.web.bind.annotation.RequestHeader("Authorization") String tokenHeader) {
        return getAllSentRequest(tokenHeader);
    }

    public ResponseEntity<?> getAllReceivedRequest(String tokenHeader) {
        try {
            String email = jwtService.extractEmail(tokenHeader);
            Optional<UserDetails> userDetailsOpt = authService.findByEmailIgnoreCase(email);
            if (userDetailsOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(new ApiResponse<>(false, "User not found", null));
            }
            UserDetails userDetails = userDetailsOpt.get();
            List<ExchangeRequestCardDTO> response = new ArrayList<>();
            List<ExchangeRequest> receivedRequests = exchangeRequestsService.findAllReceiver(userDetails.getId());
            for (var request : receivedRequests) {
                Optional<UserDetails> sender = authService.findByUserId(request.getSenderId());
                Optional<UserDetails> receiver = authService.findByUserId(request.getReceiverId());
                if (sender.isPresent() && receiver.isPresent()) {
                    response.add(new ExchangeRequestCardDTO(request, sender.get(), receiver.get()));
                }
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Successfully fetched all received requests", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse<>(false, "Failed to fetch received requests", e.getMessage()));
        }
    }

    public ResponseEntity<?> updateSwapRequest(String requestId, Map<String, Object> reqBody, String tokenHeader) {
        try {
            if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(new ApiResponse<>(false, "Missing token", null));
            }
            String token = tokenHeader.substring(7);
            if (!jwtService.validateTokenWithEmail(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .body(new ApiResponse<>(false, "Invalid token", null));
            }
            Boolean status = Boolean.parseBoolean(String.valueOf(reqBody.get("status")));
            Object offeredObj = reqBody.get("offeredSkill");
            String offeredSkill = (offeredObj != null) ? offeredObj.toString() : "";
            Optional<ExchangeRequest> optionalRequest = exchangeRequestsService.findById(requestId);
            if (optionalRequest.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(new ApiResponse<>(false, "No swap request present in DB", null));
            }
            ExchangeRequest request = optionalRequest.get();
            if (status) {
                request.setOfferedSkill(offeredSkill);
                request.setUpdatedAt(LocalDateTime.now());
                request.setStatus("Accepted");
                exchangeRequestsService.saveExchangeRequest(request);
            } else {
                request.setUpdatedAt(LocalDateTime.now());
                request.setStatus("Rejected");
                exchangeRequestsService.saveExchangeRequest(request);
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Successfully updated request", request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse<>(false, "Failed to update request", e.getMessage()));
        }
    }
}
