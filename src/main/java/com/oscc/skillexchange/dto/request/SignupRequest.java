package com.oscc.skillexchange.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

// ==================== Request DTOs ====================
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one uppercase, one lowercase, and one digit")
    private String password;

    private List<String> skillsOffered; // What they can teach
    private List<String> interests; // What they want to learn

    private String githubProfile;
    private String linkedinProfile;
    private String youtubeProfile;
    private String instagramProfile;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    private String profilePictureUrl;
    private String location;
}

