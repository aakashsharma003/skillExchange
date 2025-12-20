package com.oscc.skillexchange.controller;

import com.oscc.skillexchange.domain.entity.ChatRoom;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.request.ChatRoomRequest;
import com.oscc.skillexchange.dto.response.ApiResponse;
import com.oscc.skillexchange.dto.response.ChatMessageResponse;
import com.oscc.skillexchange.dto.response.ChatRoomResponse;
import com.oscc.skillexchange.service.AuthService;
import com.oscc.skillexchange.service.ChatService;
import com.oscc.skillexchange.service.UserService;
import com.oscc.skillexchange.util.AppConstants;
import com.oscc.skillexchange.util.EntityMapper;
import com.oscc.skillexchange.util.TokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;
    private final AuthService authService;
    private final UserService userService;
    private final EntityMapper mapper;

    @Operation(summary = "Get user's chat rooms")
    @GetMapping(value = "/rooms", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getChatRooms(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader) {
        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);
        List<ChatRoomResponse> rooms = chatService.getUserChatRooms(user.getId());
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @Operation(summary = "Create or get chat room")
    @PostMapping(value = "/rooms", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ChatRoom>> createChatRoom(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader,
            @RequestBody ChatRoomRequest request) { // Changed to @RequestBody

        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);

        // Use the data from the DTO
        ChatRoom room = chatService.createOrGetChatRoom(
                user.getId(),
                request.getUser2Id(),
                request.getExchangeRequestId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(room));
    }

    // --- METHOD 1: Fetch ALL Chat Rooms for a specific User ---
    // This endpoint returns a LIST because the sidebar needs to show all people the user talks to.
    @Operation(summary = "Get all chat rooms for a user")
    @GetMapping(value = "/rooms/{userId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getAllUserChats(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader,
            @PathVariable String userId) {

        // Extract and validate the logged-in user from the token
        String token = TokenUtil.extractToken(authHeader);
        User currentUser = authService.validateTokenAndGetUser(token);

        // Security: Ensure users can only fetch their own chat list
        if (!currentUser.getId().equals(userId)) {
            log.warn("User {} tried to access chats of user {}", currentUser.getId(), userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Unauthorized to view these chats"));
        }

        // Fetch rooms from database where this user is either Sender OR Receiver
        List<ChatRoomResponse> rooms = chatService.getUserChatRooms(userId);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }
    @Operation(summary = "Get specific chat room details")
    @GetMapping(value = "/room-details/{roomId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader,
            @PathVariable String roomId) {

        String token = TokenUtil.extractToken(authHeader);
        User currentUser = authService.validateTokenAndGetUser(token);

        // 1. Database se room entity fetch
        ChatRoom roomEntity = chatService.getChatRoomById(roomId);

        // 2. Security Check: Kya current user is room ka part hai?
        if (!roomEntity.getSenderId().equals(currentUser.getId()) &&
                !roomEntity.getReceiverId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied to this chat room"));
        }

        // 3. Logic to find the "other user" (jo current user nahi hai)
        String otherUserId = roomEntity.getSenderId().equals(currentUser.getId())
                ? roomEntity.getReceiverId()
                : roomEntity.getSenderId();

        User otherUser = userService.getUserById(otherUserId);

        // 4. FIX: Entity ko Response DTO mein convert karein using mapper
        ChatRoomResponse response = mapper.toChatRoomResponse(roomEntity, otherUser);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
    // --- METHOD 2: Fetch Messages for a Specific Room ---
// Used when a user clicks on a chat in the sidebar.
    @Operation(summary = "Get chat messages")
    @GetMapping(value = "/rooms/{roomId}/messages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @RequestHeader(AppConstants.AUTHORIZATION_HEADER) String authHeader,
            @PathVariable String roomId,
            @RequestParam(required = false) Long after) {

        String token = TokenUtil.extractToken(authHeader);
        User user = authService.validateTokenAndGetUser(token);

        // Verify the user is a participant of this specific room before showing messages
        if (!chatService.hasAccessToChatRoom(user.getId(), roomId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied to these messages"));
        }

        // Fetch messages; if 'after' timestamp is provided, get only new messages
        List<ChatMessageResponse> messages = (after != null)
                ? chatService.getMessagesAfter(roomId, Instant.ofEpochMilli(after))
                : chatService.getChatMessages(roomId);

        return ResponseEntity.ok(ApiResponse.success(messages));
    }
}
