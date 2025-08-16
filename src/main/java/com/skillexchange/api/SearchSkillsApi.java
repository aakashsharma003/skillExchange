package com.skillexchange.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/user", produces = APPLICATION_JSON_VALUE)
@Tag(name = "User Search", description = "APIs to discover skills and users")
public interface SearchSkillsApi {

    @Operation(summary = "Get all distinct skills")
    @ApiResponse(responseCode = "200", description = "Skills fetched successfully")
    @GetMapping("/all-skills")
    ResponseEntity<?> getAllSkills();

    @Operation(summary = "Search users by skill (requires Bearer token)")
    @ApiResponse(responseCode = "200", description = "Users fetched successfully")
    @ApiResponse(responseCode = "400", description = "Missing or invalid Authorization header")
    @ApiResponse(responseCode = "401", description = "Invalid token")
    @GetMapping("/search-user")
    ResponseEntity<?> getAllUsersBySkill(
        @RequestHeader("Authorization") String tokenHeader,
        @RequestParam(required = false) String skill
    );
}

