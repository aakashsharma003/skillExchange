package com.skillexchange.controller;

import com.skillexchange.model.ApiResponse;
import com.skillexchange.model.UserDetails;
import com.skillexchange.api.SearchSkillsApi;
import com.skillexchange.service.JwtService;
import com.skillexchange.service.SearchSkillsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchSkillsController implements SearchSkillsApi {

    private final SearchSkillsService skillsSearchService;
    private final JwtService jwtService;

    public SearchSkillsController(SearchSkillsService skillsSearchService, JwtService jwtService) {
        this.skillsSearchService = skillsSearchService;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<?> getAllSkills() {
        List<String> skills = skillsSearchService.getAllDistinctSkills();
        return ResponseEntity.ok(new ApiResponse<>(true, "Skills fetched successfully", skills));
    }

    @Override
    public ResponseEntity<?> getAllUsersBySkill(String tokenHeader, String skill) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ApiResponse<>(false, "Missing or invalid Authorization header", null));
        }

        String token = tokenHeader.substring(7);
        if (!jwtService.validateTokenWithEmail(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new ApiResponse<>(false, "Invalid token", null));
        }

        String email = jwtService.extractEmail(token);

        List<UserDetails> users = (skill != null && !skill.isBlank())
                                  ? skillsSearchService.findBySkillsContaining(skill, email)
                                  : skillsSearchService.getAllUsers(email);

        String message = (skill != null && !skill.isBlank())
                         ? "Matching skilled users fetched successfully"
                         : "All users fetched successfully";

        return ResponseEntity.ok(new ApiResponse<>(true, message, users));
    }
}
