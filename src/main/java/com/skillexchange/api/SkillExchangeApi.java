package com.skillexchange.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import com.skillexchange.model.request.ExchangeRequest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/skill-swap", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Skill Swap", description = "APIs to create, list, and update skill swap requests")
public interface SkillExchangeApi {

    @Operation(summary = "Create a skill swap request")
    @ApiResponse(responseCode = "200", description = "Swap request saved successfully")
    @ApiResponse(responseCode = "400", description = "Missing or invalid token")
    @ApiResponse(responseCode = "401", description = "Unauthorized (invalid token)")
    @PostMapping(path = "/request", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<?> swapRequest(
        @RequestHeader("Authorization") String tokenHeader,
        @RequestBody ExchangeRequest swapDetails
    );

    @Operation(summary = "Get all swap requests for the authenticated user (sent + received)")
    @ApiResponse(responseCode = "200", description = "Requests fetched successfully")
    @ApiResponse(responseCode = "400", description = "Missing or invalid token")
    @ApiResponse(responseCode = "401", description = "Unauthorized (invalid token)")
    @GetMapping("/get-all-request")
    ResponseEntity<?> getAllRequest(@RequestHeader("Authorization") String tokenHeader);

    @Operation(summary = "Get all sent swap requests for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Sent requests fetched successfully")
    @ApiResponse(responseCode = "400", description = "Missing or invalid token")
    @ApiResponse(responseCode = "401", description = "Unauthorized (invalid token)")
    @GetMapping("/sent-requests")
    ResponseEntity<?> getAllSentRequest(@RequestHeader("Authorization") String tokenHeader);

    @Operation(summary = "Get all received swap requests for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Received requests fetched successfully")
    @ApiResponse(responseCode = "400", description = "Missing or invalid token")
    @ApiResponse(responseCode = "401", description = "Unauthorized (invalid token)")
    @GetMapping("/received-requests")
    ResponseEntity<?> getAllReceivedRequest(@RequestHeader("Authorization") String tokenHeader);

    @Operation(summary = "Update a swap request (accept/reject, optionally set offeredSkill)")
    @ApiResponse(responseCode = "200", description = "Request updated successfully")
    @ApiResponse(responseCode = "400", description = "Bad request or request not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized (invalid token)")
    @PutMapping(path = "/update-request/{id}", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<?> updateSwapRequest(
        @PathVariable("id") String requestId,
        @RequestBody Map<String, Object> reqBody,
        @RequestHeader("Authorization") String tokenHeader
    );
}

