package com.skillexchange.controller;

import com.skillexchange.api.ChatApi;
import com.skillexchange.model.ApiResponse;
import com.skillexchange.model.ChatMessage;
import com.skillexchange.model.ChatRoom;
import com.skillexchange.service.ChatService;
import com.skillexchange.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@org.springframework.web.bind.annotation.RequestMapping("/chat")
public class ChatController implements ChatApi {

    private final ChatService chatService;
    private final JwtService jwtService;

    public ChatController(ChatService chatService, JwtService jwtService) {
        this.chatService = chatService;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<?> getUserChatRooms(String userId, String tokenHeader) {
        String email = jwtService.extractEmail(tokenHeader);
        return ResponseEntity.ok(new ApiResponse<>(true, "Chat rooms fetched successfully",
                chatService.getUserChatRooms(userId, email)));
    }

    @Override
    public ResponseEntity<?> createChatRoom(ChatRoom chatRoom) {
        chatService.createChatRoom(chatRoom);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "Chat room created successfully", chatRoom));
    }

    @Override
    public ResponseEntity<?> getChatMessages(String chatRoomId) {
        List<ChatMessage> msgs = chatService.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Messages fetched successfully", msgs));
    }

    // ----- Aliases to match client expectations -----

    @GetMapping("/rooms/{userId}")
    public ResponseEntity<?> aliasUserRooms(@PathVariable String userId,
                                            @RequestHeader("Authorization") String tokenHeader) {
        return getUserChatRooms(userId, tokenHeader);
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<?> aliasRoomMessages(@PathVariable String chatRoomId) {
        return getChatMessages(chatRoomId);
    }

    // Some clients may POST to fetch data; support POST aliases that delegate to the same handlers
    @org.springframework.web.bind.annotation.PostMapping("/rooms/{userId}")
    public ResponseEntity<?> aliasUserRoomsPost(@PathVariable String userId,
                                                @RequestHeader("Authorization") String tokenHeader) {
        return getUserChatRooms(userId, tokenHeader);
    }

    @org.springframework.web.bind.annotation.PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<?> aliasRoomMessagesPost(@PathVariable String chatRoomId) {
        return getChatMessages(chatRoomId);
    }
}

