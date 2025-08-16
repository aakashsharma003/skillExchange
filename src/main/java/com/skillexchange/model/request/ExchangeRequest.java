package com.skillexchange.model.request;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_requests", schema = "skillexchange")
@Data
public class ExchangeRequest {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Column(name = "requested_skill", nullable = false)
    private String requestedSkill;

    @Column(name = "offered_skill")
    private String offeredSkill;

    @Column(name = "message")
    private String message;

    @Column(name = "status", nullable = false)
    private String status = "Pending";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
