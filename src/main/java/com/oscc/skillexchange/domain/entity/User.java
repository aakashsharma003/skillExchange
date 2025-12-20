package com.oscc.skillexchange.domain.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "email_idx", def = "{'email': 1}", unique = true)
public class User {

    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true, name = "email_idx")
    private String email;

    private String phone;

    private String password;

    @Indexed
    private List<String> skills = new ArrayList<>();

    private String githubProfile;
    private String linkedinProfile;
    private String youtubeProfile;
    private String instagramProfile;
    private String bio;
    private String profilePictureUrl;
    private String location;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private boolean enabled = true;
    private boolean locked = false;
}
