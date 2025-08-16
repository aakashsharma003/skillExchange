package com.skillexchange.model.dto;

import java.util.List;
import java.util.UUID;

import com.skillexchange.model.ChatMessage;
import com.skillexchange.model.UserDetails;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatThreadDTO {
    private UUID chatRoomId;
    private UserDetails user;
    private String requestedSkill;
    private String offeredSkill;
    private List<ChatMessage> messages;
}
