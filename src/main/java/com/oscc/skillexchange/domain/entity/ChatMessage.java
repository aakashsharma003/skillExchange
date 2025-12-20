package com.oscc.skillexchange.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "room_created_idx",
        def = "{'chatRoomId': 1, 'createdAt': 1}")
public class ChatMessage {

    @Id
    private String id;

    @Indexed
    private String chatRoomId;

    private String senderEmail;
    private String senderId;
    private String receiverId;

    private String content;

    @Indexed
    @CreatedDate
    private Instant createdAt;
}
