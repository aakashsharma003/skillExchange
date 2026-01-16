package com.oscc.skillexchange.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Document(collection = "scheduled_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledSession {

    @Id
    private String id;

    @Indexed
    private String chatRoomId;

    @Indexed
    private String exchangeRequestId;

    @Indexed
    private String userId1; // Teacher

    @Indexed
    private String userId2; // Learner

    private String skill; // Skill being taught/learned

    private LocalDateTime scheduledDateTime;

    private String title;

    private String description;

    private SessionStatus status = SessionStatus.SCHEDULED;

    @Indexed
    private Instant sessionDate; // Date only for filtering

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum SessionStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
