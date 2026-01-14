package com.oscc.skillexchange.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private List<String> skillsOffered; // What they can teach
    private List<String> interests; // What they want to learn
    private Map<String, Double> learningProgress; // Skill -> Proficiency (0-100)
    private String githubProfile;
    private String linkedinProfile;
    private String youtubeProfile;
    private String instagramProfile;
    private String bio;
    private String location;
    private String profilePictureUrl;
    private Instant createdAt;
}
