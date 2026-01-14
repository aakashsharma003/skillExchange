package com.oscc.skillexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscc.skillexchange.domain.entity.ChatRoom;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.request.ChatRoomRequest;
import com.oscc.skillexchange.dto.response.ApiResponse;
import com.oscc.skillexchange.dto.response.ChatMessageResponse;
import com.oscc.skillexchange.dto.response.ChatRoomResponse;
import com.oscc.skillexchange.service.AuthService;
import com.oscc.skillexchange.service.ChatService;
import com.oscc.skillexchange.util.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @MockBean
    private AuthService authService;

    private static final String AUTH_HEADER_VALUE = "Bearer test-token";

    @Test
    void testGetChatRooms_shouldReturnUsersChatRooms() throws Exception {
        User user = User.builder()
                .id("user-1")
                .email("john@example.com")
                .build();

        ChatRoomResponse room1 = ChatRoomResponse.builder()
                .id("room-1")
                .chatRoomId("room-1")
                .otherUser(com.oscc.skillexchange.dto.response.UserResponse.builder()
                        .id("user-2")
                        .fullName("Jane")
                        .build())
                .lastActivityAt(Instant.now())
                .build();

        ChatRoomResponse room2 = ChatRoomResponse.builder()
                .id("room-2")
                .chatRoomId("room-2")
                .otherUser(com.oscc.skillexchange.dto.response.UserResponse.builder()
                        .id("user-3")
                        .fullName("Bob")
                        .build())
                .lastActivityAt(Instant.now())
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(chatService.getUserChatRooms("user-1")).thenReturn(List.of(room1, room2));

        mockMvc.perform(get("/api/chat/rooms")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testCreateChatRoom_shouldCreateNewRoom() throws Exception {
        User user = User.builder()
                .id("user-1")
                .email("john@example.com")
                .build();

        ChatRoomRequest request = new ChatRoomRequest();
        request.setUser1Id("user-1");
        request.setUser2Id("user-2");
        request.setExchangeRequestId("req-1");

        ChatRoom chatRoom = ChatRoom.builder()
                .id("room-1")
                .senderId("user-1")
                .receiverId("user-2")
                .exchangeRequestId("req-1")
                .lastActivityAt(Instant.now())
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(chatService.createOrGetChatRoom("user-1", "user-2", "req-1")).thenReturn(chatRoom);

        mockMvc.perform(post("/api/chat/rooms")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("room-1"));
    }

    @Test
    void testGetAllUserChats_shouldReturnUsersChatsList() throws Exception {
        User user = User.builder()
                .id("user-1")
                .email("john@example.com")
                .build();

        ChatRoomResponse room = ChatRoomResponse.builder()
                .id("room-1")
                .chatRoomId("room-1")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(chatService.getUserChatRooms("user-1")).thenReturn(List.of(room));

        mockMvc.perform(get("/api/chat/rooms/user-1")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testGetAllUserChats_shouldReturnForbiddenForDifferentUser() throws Exception {
        User currentUser = User.builder()
                .id("user-1")
                .email("john@example.com")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(currentUser);

        mockMvc.perform(get("/api/chat/rooms/user-2")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetChatRoomDetails_shouldReturnRoomDetails() throws Exception {
        User user = User.builder()
                .id("user-1")
                .build();

        ChatRoomResponse room = ChatRoomResponse.builder()
                .id("room-1")
                .chatRoomId("room-1")
                .offeredSkill("Java")
                .requestedSkill("Python")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(chatService.getChatRoomById("room-1")).thenReturn(
                ChatRoom.builder()
                        .id("room-1")
                        .senderId("user-1")
                        .receiverId("user-2")
                        .build()
        );
        when(chatService.getChatRoomById(any())).thenReturn(
                ChatRoom.builder()
                        .id("room-1")
                        .senderId("user-1")
                        .receiverId("user-2")
                        .build()
        );

        mockMvc.perform(get("/api/chat/room-details/room-1")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetMessages_shouldReturnChatMessages() throws Exception {
        User user = User.builder()
                .id("user-1")
                .build();

        ChatMessageResponse msg1 = ChatMessageResponse.builder()
                .id("msg-1")
                .chatRoomId("room-1")
                .content("Hello!")
                .build();

        ChatMessageResponse msg2 = ChatMessageResponse.builder()
                .id("msg-2")
                .chatRoomId("room-1")
                .content("Hi there!")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(chatService.hasAccessToChatRoom("user-1", "room-1")).thenReturn(true);
        when(chatService.getChatMessages("room-1")).thenReturn(List.of(msg1, msg2));

        mockMvc.perform(get("/api/chat/rooms/room-1/messages")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].content").value("Hello!"));
    }

    @Test
    void testGetMessages_shouldReturnForbiddenForUnauthorizedUser() throws Exception {
        User user = User.builder()
                .id("user-1")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(chatService.hasAccessToChatRoom("user-1", "room-1")).thenReturn(false);

        mockMvc.perform(get("/api/chat/rooms/room-1/messages")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetMessages_shouldReturnMessagesAfterTimestamp() throws Exception {
        User user = User.builder()
                .id("user-1")
                .build();

        long afterTimestamp = System.currentTimeMillis() - 60000;

        ChatMessageResponse msg = ChatMessageResponse.builder()
                .id("msg-1")
                .chatRoomId("room-1")
                .content("Recent message")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(chatService.hasAccessToChatRoom("user-1", "room-1")).thenReturn(true);
        when(chatService.getMessagesAfter(eq("room-1"), any())).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/chat/rooms/room-1/messages")
                .param("after", String.valueOf(afterTimestamp))
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value("Recent message"));
    }
}
