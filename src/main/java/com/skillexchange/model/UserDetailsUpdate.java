package com.skillexchange.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserDetailsUpdate {
    private String name;
    private List<String> skills = new ArrayList<>();
    private String phone;
    private String githubProfile;
    private String linkedinProfile;
    private String youtubeProfile;
    private String instagramProfile;
    private String bio;
    private String location;
    private String profilePictureUrl;

    public UserDetailsUpdate(String name, String phone, List<String> skills,
                             String location, String bio, String instagramProfile, String youtubeProfile, String linkedinProfile, String githubProfile, String profilePictureUrl) {
        this.name = name;
        this.skills = skills;
        this.phone = phone;
        this.location = location;
        this.bio = bio;
        this.instagramProfile = instagramProfile;
        this.youtubeProfile = youtubeProfile;
        this.linkedinProfile = linkedinProfile;
        this.githubProfile = githubProfile;
        this.profilePictureUrl = profilePictureUrl;
    }

}
