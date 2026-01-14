package com.oscc.skillexchange.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String type; // MATCH, LEARNING_PROGRESS, etc.
    private String title;
    private String message;
    private String userId;
    private Long timestamp;
}
