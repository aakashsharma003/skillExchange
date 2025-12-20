package com.oscc.skillexchange.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "exchange_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "sender_receiver_skill_idx",
        def = "{'senderId': 1, 'receiverId': 1, 'requestedSkill': 1}")
public class ExchangeRequest {

    @Id
    private String id;

    @Indexed
    private String senderId;

    @Indexed
    private String receiverId;

    private String requestedSkill;
    private String offeredSkill;
    private String message;

    @Indexed
    private RequestStatus status = RequestStatus.PENDING;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum RequestStatus {
        PENDING, ACCEPTED, REJECTED, CANCELLED
    }
}
