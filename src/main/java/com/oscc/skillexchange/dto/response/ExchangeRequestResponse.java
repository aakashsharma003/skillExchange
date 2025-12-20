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
public class ExchangeRequestResponse {
    private String id;
    private String requestedSkill;
    private String offeredSkill;
    private String message;
    private String status;
    private UserResponse sender;
    private UserResponse receiver;
    private Instant createdAt;
    private Instant updatedAt;
}
