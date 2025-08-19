package com.skillexchange.model;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "users", schema = "skillexchange")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    @Column(name = "full_name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150, unique = true)
    private String email;
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "skills", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] skills;

    @Column(name = "github_profile", length = 200)
    private String githubProfile;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "profile_picture_url", length = 300)
    private String profilePictureUrl;

    @Column(name = "linkedin_profile", length = 300)
    private String linkedinProfile;

    @Column(name = "youtube_profile", length = 300)
    private String youtubeProfile;

    @Column(name = "instagram_profile", length = 300)
    private String instagramProfile;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
