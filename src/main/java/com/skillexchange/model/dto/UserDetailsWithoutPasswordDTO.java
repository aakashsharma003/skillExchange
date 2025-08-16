package com.skillexchange.model.dto;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsWithoutPasswordDTO {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private List<String> skills ;
    private String githubProfile;
    private String linkedinProfile;
    private String youtubeProfile;
    private String instagramProfile;
    private String bio;
    private String location;
    private String profilePictureUrl;
}
