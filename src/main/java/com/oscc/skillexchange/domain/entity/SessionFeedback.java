package com.oscc.skillexchange.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "session_feedbacks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionFeedback {

    @Id
    private String id;

    @Indexed
    private String scheduledSessionId;

    @Indexed
    private String sessionUserId; // User who provided feedback

    @Indexed
    private String otherUserId; // The other user in the session

    private String skill; // Skill that was exchanged

    private Integer rating; // 1-5

    private String comment;

    private Boolean skillExchanged; // Whether skill was successfully exchanged

    @CreatedDate
    private Instant createdAt;
}
