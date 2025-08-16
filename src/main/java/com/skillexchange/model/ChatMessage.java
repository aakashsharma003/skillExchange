package com.skillexchange.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_messages")
@NoArgsConstructor
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "chat_room_id", nullable = false)
    private UUID chatRoomId;

    @Column(name = "sender_email", nullable = false)
    private String senderEmail;

    @Transient
    private UUID senderId;
    @Transient
    private UUID receiverId;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Optionally, add a constructor for convenience
    public ChatMessage(UUID chatRoomId, String senderEmail, String content, LocalDateTime createdAt) {
        this.chatRoomId = chatRoomId;
        this.senderEmail = senderEmail;
        this.content = content;
        this.createdAt = createdAt;
    }
}
