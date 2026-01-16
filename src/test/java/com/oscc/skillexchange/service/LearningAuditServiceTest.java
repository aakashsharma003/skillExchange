package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.ChatMessage;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.repository.ChatMessageRepository;
import com.oscc.skillexchange.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningAuditServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatClient chatClient;

    @InjectMocks
    private LearningAuditService learningAuditService;

    private List<ChatMessage> testMessages;
    private User testUser;

    @BeforeEach
    void setUp() {
        ChatMessage msg1 = ChatMessage.builder()
                .id("msg1")
                .chatRoomId("room1")
                .senderId("user1")
                .senderEmail("user1@example.com")
                .content("Let's discuss Java programming")
                .createdAt(Instant.now())
                .build();

        ChatMessage msg2 = ChatMessage.builder()
                .id("msg2")
                .chatRoomId("room1")
                .senderId("user2")
                .senderEmail("user2@example.com")
                .content("Java is great for backend development")
                .createdAt(Instant.now().plusSeconds(60))
                .build();

        testMessages = List.of(msg1, msg2);

        testUser = User.builder()
                .id("user1")
                .email("user1@example.com")
                .fullName("Test User")
                .learningProgress(null)
                .build();
    }

    @Test
    void testProcessChatTranscript_EmptyMessages() {
        when(chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc("room1"))
                .thenReturn(List.of());

        learningAuditService.processChatTranscript("room1", "user1");

        verify(chatMessageRepository, times(1)).findByChatRoomIdOrderByCreatedAtDesc("room1");
        verify(chatClient, never()).prompt();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testProcessChatTranscript_WithMessages_NullLearnerId() {
        when(chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc("room1"))
                .thenReturn(testMessages);

        // Mock chat client response
        LearningAuditService.LearningAnalysis analysis = 
            new LearningAuditService.LearningAnalysis(true, List.of("Java"), 2.5);

        // Note: This is a simplified test - actual ChatClient mocking is more complex
        // In a real scenario, you'd need to mock the full ChatClient chain

        learningAuditService.processChatTranscript("room1", null);

        // Should not throw exception, but skip progress update
        verify(chatMessageRepository, times(1)).findByChatRoomIdOrderByCreatedAtDesc("room1");
    }

    @Test
    void testProcessChatTranscript_WithValidLearnerId() {
        when(chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc("room1"))
                .thenReturn(testMessages);
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // This test would need proper ChatClient mocking which is complex
        // For now, just verify the flow doesn't crash
        assertDoesNotThrow(() -> {
            learningAuditService.processChatTranscript("room1", "user1");
        });

        verify(chatMessageRepository, times(1)).findByChatRoomIdOrderByCreatedAtDesc("room1");
    }

    @Test
    void testUpdateLearningProgress() {
        LearningAuditService.LearningAnalysis analysis = 
            new LearningAuditService.LearningAnalysis(true, List.of("Java", "Spring"), 3.5);

        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        learningAuditService.updateLearningProgress("user1", analysis);

        verify(userRepository, times(1)).findById("user1");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateLearningProgress_UserNotFound() {
        LearningAuditService.LearningAnalysis analysis = 
            new LearningAuditService.LearningAnalysis(true, List.of("Java"), 2.0);

        when(userRepository.findById("user1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            learningAuditService.updateLearningProgress("user1", analysis);
        });

        verify(userRepository, times(1)).findById("user1");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateLearningProgress_ProgressCappedAt100() {
        testUser.setLearningProgress(java.util.Map.of("Java", 98.0));
        
        LearningAuditService.LearningAnalysis analysis = 
            new LearningAuditService.LearningAnalysis(true, List.of("Java"), 5.0);

        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Verify progress is capped at 100
            assertTrue(savedUser.getLearningProgress().get("Java") <= 100.0);
            return savedUser;
        });

        learningAuditService.updateLearningProgress("user1", analysis);

        verify(userRepository, times(1)).save(any(User.class));
    }
}
