package com.skillexchange.api;

import com.skillexchange.model.ChatRoom;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/chat", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Chat", description = "APIs for chat rooms and messages")
public interface ChatApi {

    @Operation(summary = "Get chat rooms for a user")
    @ApiResponse(responseCode = "200", description = "Chat rooms fetched successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized access")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping(path = "/users/{userId}/rooms")
    ResponseEntity<?> getUserChatRooms(
        @NotBlank(message = "'userId' is required")
        @PathVariable("userId") String userId,
        @RequestHeader("Authorization") String tokenHeader
    );

    @Operation(summary = "Create a chat room")
    @ApiResponse(responseCode = "201", description = "Chat room created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized access")
    @ApiResponse(responseCode = "409", description = "Chat room already exists")
    @PostMapping(path = "/create-chat-room", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<?> createChatRoom(@Valid @RequestBody ChatRoom chatRoom);

    @Operation(summary = "Get messages for a chat room")
    @ApiResponse(responseCode = "200", description = "Messages fetched successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized access")
    @ApiResponse(responseCode = "404", description = "Chat room not found")
    @GetMapping(path = "/room/messages")
    ResponseEntity<?> getChatMessages(
        @NotBlank(message = "'chatRoomId' is required")
        @RequestParam String chatRoomId
    );
}