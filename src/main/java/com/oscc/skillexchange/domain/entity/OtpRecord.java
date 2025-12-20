package com.oscc.skillexchange.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Document(collection = "otp_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRecord {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String otp;

    @Indexed(expireAfterSeconds = 300) // 5 minutes TTL
    private Instant expiresAt;

    @CreatedDate
    private Instant createdAt;
}

