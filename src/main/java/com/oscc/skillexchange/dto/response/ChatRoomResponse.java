package com.oscc.skillexchange.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private String id;
    // Convenience alias for frontend
    private String chatRoomId;
    private UserResponse otherUser;
    private String exchangeRequestId;
    private String offeredSkill;
    private String requestedSkill;
    private Instant lastActivityAt;
}
