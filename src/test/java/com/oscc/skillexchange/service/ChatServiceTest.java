package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.ChatMessage;
import com.oscc.skillexchange.domain.entity.ChatRoom;
import com.oscc.skillexchange.domain.entity.ExchangeRequest;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.response.ChatMessageResponse;
import com.oscc.skillexchange.dto.response.ChatRoomResponse;
import com.oscc.skillexchange.exception.ResourceNotFoundException;
import com.oscc.skillexchange.repository.ChatMessageRepository;
import com.oscc.skillexchange.repository.ChatRoomRepository;
import com.oscc.skillexchange.repository.ExchangeRequestRepository;
import com.oscc.skillexchange.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private UserService userService;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private ChatService chatService;

    private ChatRoom testChatRoom;
    private ChatMessage testMessage;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = User.builder()
                .id("user-1")
                .fullName("John Doe")
                .email("john@example.com")
                .build();

        testChatRoom = ChatRoom.builder()
                .id("room-1")
                .senderId("user-1")
                .receiverId("user-2")
                .exchangeRequestId("req-1")
                .lastActivityAt(Instant.now())
                .build();

        testMessage = ChatMessage.builder()
                .id("msg-1")
                .chatRoomId("room-1")
                .senderId("user-1")
                .receiverId("user-2")
                .content("Hello!")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void testCreateOrGetChatRoom_shouldCreateNewRoom() {
        when(chatRoomRepository.findByParticipants("user-1", "user-2")).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

        ChatRoom result = chatService.createOrGetChatRoom("user-1", "user-2", "req-1");

        assertNotNull(result);
        assertEquals("room-1", result.getId());
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    void testCreateOrGetChatRoom_shouldReturnExistingRoom() {
        when(chatRoomRepository.findByParticipants("user-1", "user-2")).thenReturn(Optional.of(testChatRoom));

        ChatRoom result = chatService.createOrGetChatRoom("user-1", "user-2", "req-1");

        assertNotNull(result);
        assertEquals("room-1", result.getId());
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    void testGetChatRoomById_shouldReturnRoom() {
        when(chatRoomRepository.findById("room-1")).thenReturn(Optional.of(testChatRoom));

        ChatRoom result = chatService.getChatRoomById("room-1");

        assertNotNull(result);
        assertEquals("room-1", result.getId());
    }

    @Test
    void testGetChatRoomById_shouldThrowExceptionWhenNotFound() {
        when(chatRoomRepository.findById("non-existing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chatService.getChatRoomById("non-existing"));
    }

    @Test
    void testGetUserChatRooms_shouldReturnAllRoomsForUser() {
        ChatRoom room2 = ChatRoom.builder()
                .id("room-2")
                .senderId("user-1")
                .receiverId("user-3")
                .lastActivityAt(Instant.now())
                .build();

        when(chatRoomRepository.findByUserIdOrderByLastActivityAtDesc("user-1"))
                .thenReturn(List.of(testChatRoom, room2));

        User otherUser1 = User.builder().id("user-2").fullName("User 2").build();
        User otherUser2 = User.builder().id("user-3").fullName("User 3").build();

        when(userService.getUserById("user-2")).thenReturn(otherUser1);
        when(userService.getUserById("user-3")).thenReturn(otherUser2);

        ChatRoomResponse resp1 = ChatRoomResponse.builder()
                .id("room-1")
                .chatRoomId("room-1")
                .build();
        ChatRoomResponse resp2 = ChatRoomResponse.builder()
                .id("room-2")
                .chatRoomId("room-2")
                .build();

        when(mapper.toChatRoomResponse(testChatRoom, otherUser1)).thenReturn(resp1);
        when(mapper.toChatRoomResponse(room2, otherUser2)).thenReturn(resp2);

        when(exchangeRequestRepository.findById("req-1")).thenReturn(Optional.empty());

        List<ChatRoomResponse> results = chatService.getUserChatRooms("user-1");

        assertEquals(2, results.size());
        verify(chatRoomRepository).findByUserIdOrderByLastActivityAtDesc("user-1");
    }

    @Test
    void testSaveMessage_shouldSaveMessageAndUpdateRoom() {
        when(chatRoomRepository.findById("room-1")).thenReturn(Optional.of(testChatRoom));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);
        when(mapper.toChatMessageResponse(testMessage)).thenReturn(
                ChatMessageResponse.builder()
                        .id("msg-1")
                        .chatRoomId("room-1")
                        .content("Hello!")
                        .build()
        );

        ChatMessageResponse result = chatService.saveMessage(testMessage);

        assertNotNull(result);
        assertEquals("msg-1", result.getId());
        
        ArgumentCaptor<ChatMessage> msgCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(msgCaptor.capture());
        assertNotNull(msgCaptor.getValue().getCreatedAt());

        ArgumentCaptor<ChatRoom> roomCaptor = ArgumentCaptor.forClass(ChatRoom.class);
        verify(chatRoomRepository).save(roomCaptor.capture());
        assertNotNull(roomCaptor.getValue().getLastActivityAt());
    }

    @Test
    void testSaveMessage_shouldThrowExceptionWhenRoomNotFound() {
        ChatMessage message = ChatMessage.builder()
                .chatRoomId("non-existing")
                .senderId("user-1")
                .receiverId("user-2")
                .content("Hello!")
                .build();

        when(chatRoomRepository.findById("non-existing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chatService.saveMessage(message));
    }

    @Test
    void testSaveMessage_shouldThrowExceptionWhenSenderIdMissing() {
        ChatMessage message = ChatMessage.builder()
                .chatRoomId("room-1")
                .receiverId("user-2")
                .content("Hello!")
                .build();

        when(chatRoomRepository.findById("room-1")).thenReturn(Optional.of(testChatRoom));

        assertThrows(IllegalArgumentException.class, () -> chatService.saveMessage(message));
    }

    @Test
    void testGetChatMessages_shouldReturnMessagesForRoom() {
        ChatMessage msg2 = ChatMessage.builder()
                .id("msg-2")
                .chatRoomId("room-1")
                .content("Hi there!")
                .createdAt(Instant.now())
                .build();

        when(chatRoomRepository.findById("room-1")).thenReturn(Optional.of(testChatRoom));
        when(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc("room-1"))
                .thenReturn(List.of(testMessage, msg2));
        when(mapper.toChatMessageResponseList(any())).thenReturn(
                List.of(
                        ChatMessageResponse.builder().id("msg-1").build(),
                        ChatMessageResponse.builder().id("msg-2").build()
                )
        );

        List<ChatMessageResponse> results = chatService.getChatMessages("room-1");

        assertEquals(2, results.size());
        verify(chatMessageRepository).findByChatRoomIdOrderByCreatedAtAsc("room-1");
    }

    @Test
    void testGetMessagesAfter_shouldReturnMessagesAfterTimestamp() {
        Instant afterTime = Instant.now().minusSeconds(60);

        when(chatMessageRepository.findByChatRoomIdAndCreatedAtAfter("room-1", afterTime))
                .thenReturn(List.of(testMessage));
        when(mapper.toChatMessageResponseList(any())).thenReturn(
                List.of(ChatMessageResponse.builder().id("msg-1").build())
        );

        List<ChatMessageResponse> results = chatService.getMessagesAfter("room-1", afterTime);

        assertEquals(1, results.size());
        verify(chatMessageRepository).findByChatRoomIdAndCreatedAtAfter("room-1", afterTime);
    }

    @Test
    void testHasAccessToChatRoom_shouldReturnTrueForParticipants() {
        when(chatRoomRepository.findById("room-1")).thenReturn(Optional.of(testChatRoom));

        assertTrue(chatService.hasAccessToChatRoom("user-1", "room-1"));
        assertTrue(chatService.hasAccessToChatRoom("user-2", "room-1"));
    }

    @Test
    void testHasAccessToChatRoom_shouldReturnFalseForNonParticipants() {
        when(chatRoomRepository.findById("room-1")).thenReturn(Optional.of(testChatRoom));

        assertFalse(chatService.hasAccessToChatRoom("user-3", "room-1"));
    }
}
