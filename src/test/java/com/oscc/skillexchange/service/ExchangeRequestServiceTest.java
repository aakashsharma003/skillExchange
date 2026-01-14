package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.ChatMessage;
import com.oscc.skillexchange.domain.entity.ChatRoom;
import com.oscc.skillexchange.domain.entity.ExchangeRequest;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.request.ExchangeRequestDto;
import com.oscc.skillexchange.dto.request.UpdateExchangeRequestDto;
import com.oscc.skillexchange.dto.response.ExchangeRequestResponse;
import com.oscc.skillexchange.exception.DuplicateResourceException;
import com.oscc.skillexchange.exception.ResourceNotFoundException;
import com.oscc.skillexchange.repository.ExchangeRequestRepository;
import com.oscc.skillexchange.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExchangeRequestServiceTest {

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private UserService userService;

    @Mock
    private EntityMapper mapper;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ExchangeRequestService exchangeRequestService;

    private ExchangeRequest testRequest;
    private User senderUser;
    private User receiverUser;
    private ExchangeRequestDto requestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        senderUser = User.builder()
                .id("user-1")
                .fullName("John Doe")
                .email("john@example.com")
                .build();

        receiverUser = User.builder()
                .id("user-2")
                .fullName("Jane Smith")
                .email("jane@example.com")
                .build();

        requestDto = ExchangeRequestDto.builder()
                .receiverId("jane@example.com")
                .requestedSkill("Python")
                .offeredSkill("Java")
                .message("I'd like to learn Python from you")
                .build();

        testRequest = ExchangeRequest.builder()
                .id("req-1")
                .senderId("user-1")
                .receiverId("user-2")
                .requestedSkill("Python")
                .offeredSkill("Java")
                .message("I'd like to learn Python from you")
                .status(ExchangeRequest.RequestStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void testCreateRequest_shouldCreateNewRequest() {
        when(userService.getUserByEmail("jane@example.com")).thenReturn(receiverUser);
        when(exchangeRequestRepository.findBySenderAndReceiverAndSkillAndStatusIn(
                "user-1", "user-2", "Python", 
                List.of(ExchangeRequest.RequestStatus.PENDING, ExchangeRequest.RequestStatus.ACCEPTED)
        )).thenReturn(Optional.empty());
        when(mapper.toExchangeRequest(requestDto, "user-1")).thenReturn(testRequest);
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenReturn(testRequest);
        when(userService.getUserById("user-1")).thenReturn(senderUser);
        when(mapper.toExchangeRequestResponse(testRequest, senderUser, receiverUser))
                .thenReturn(ExchangeRequestResponse.builder()
                        .id("req-1")
                        .status("PENDING")
                        .build());

        ExchangeRequestResponse response = exchangeRequestService.createRequest("user-1", requestDto);

        assertNotNull(response);
        assertEquals("req-1", response.getId());
        verify(exchangeRequestRepository).save(any(ExchangeRequest.class));
    }

    @Test
    void testCreateRequest_shouldThrowExceptionWhenReceiverNotFound() {
        when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(null);

        ExchangeRequestDto dto = ExchangeRequestDto.builder()
                .receiverId("nonexistent@example.com")
                .requestedSkill("Python")
                .build();

        assertThrows(ResourceNotFoundException.class, 
                () -> exchangeRequestService.createRequest("user-1", dto));
    }

    @Test
    void testCreateRequest_shouldThrowExceptionForDuplicateRequest() {
        when(userService.getUserByEmail("jane@example.com")).thenReturn(receiverUser);
        when(exchangeRequestRepository.findBySenderAndReceiverAndSkillAndStatusIn(
                "user-1", "user-2", "Python",
                List.of(ExchangeRequest.RequestStatus.PENDING, ExchangeRequest.RequestStatus.ACCEPTED)
        )).thenReturn(Optional.of(testRequest));

        assertThrows(DuplicateResourceException.class,
                () -> exchangeRequestService.createRequest("user-1", requestDto));
    }

    @Test
    void testGetSentRequests_shouldReturnRequestsSentByUser() {
        ExchangeRequest req2 = ExchangeRequest.builder()
                .id("req-2")
                .senderId("user-1")
                .receiverId("user-3")
                .requestedSkill("Java")
                .status(ExchangeRequest.RequestStatus.ACCEPTED)
                .createdAt(Instant.now())
                .build();

        when(exchangeRequestRepository.findBySenderIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(List.of(testRequest, req2));
        when(userService.getUserById(anyString())).thenReturn(senderUser, receiverUser, senderUser);
        when(mapper.toExchangeRequestResponse(any(), any(), any()))
                .thenReturn(ExchangeRequestResponse.builder().id("req-1").build(),
                           ExchangeRequestResponse.builder().id("req-2").build());

        List<ExchangeRequestResponse> results = exchangeRequestService.getSentRequests("user-1");

        assertEquals(2, results.size());
        verify(exchangeRequestRepository).findBySenderIdOrderByCreatedAtDesc("user-1");
    }

    @Test
    void testGetReceivedRequests_shouldReturnRequestsReceivedByUser() {
        when(exchangeRequestRepository.findByReceiverIdOrderByCreatedAtDesc("user-2"))
                .thenReturn(List.of(testRequest));
        when(userService.getUserById(anyString())).thenReturn(senderUser, receiverUser);
        when(mapper.toExchangeRequestResponse(testRequest, senderUser, receiverUser))
                .thenReturn(ExchangeRequestResponse.builder().id("req-1").build());

        List<ExchangeRequestResponse> results = exchangeRequestService.getReceivedRequests("user-2");

        assertEquals(1, results.size());
        verify(exchangeRequestRepository).findByReceiverIdOrderByCreatedAtDesc("user-2");
    }

    @Test
    void testGetAllRequestsForUser_shouldReturnSentAndReceivedRequests() {
        when(userService.getUserByEmail("john@example.com")).thenReturn(senderUser);
        when(exchangeRequestRepository.findByUserIdAndStatusIn(
                "user-1",
                List.of(ExchangeRequest.RequestStatus.values())
        )).thenReturn(List.of(testRequest));
        when(userService.getUserById(anyString())).thenReturn(senderUser, receiverUser);
        when(mapper.toExchangeRequestResponse(testRequest, senderUser, receiverUser))
                .thenReturn(ExchangeRequestResponse.builder().id("req-1").build());

        List<ExchangeRequestResponse> results = exchangeRequestService.getAllRequestsForUser("john@example.com");

        assertEquals(1, results.size());
    }

    @Test
    void testUpdateRequest_shouldAcceptRequest() {
        ChatRoom chatRoom = ChatRoom.builder()
                .id("room-1")
                .senderId("user-1")
                .receiverId("user-2")
                .build();

        UpdateExchangeRequestDto updateDto = UpdateExchangeRequestDto.builder()
                .accepted(true)
                .offeredSkill("C++")
                .build();

        when(exchangeRequestRepository.findById("req-1")).thenReturn(Optional.of(testRequest));
        when(chatService.createOrGetChatRoom("user-1", "user-2", "req-1")).thenReturn(chatRoom);
        when(chatService.saveMessage(any(ChatMessage.class))).thenReturn(null);
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenReturn(testRequest);
        when(userService.getUserById("user-1")).thenReturn(senderUser);
        when(userService.getUserById("user-2")).thenReturn(receiverUser);
        when(mapper.toExchangeRequestResponse(testRequest, senderUser, receiverUser))
                .thenReturn(ExchangeRequestResponse.builder()
                        .id("req-1")
                        .status("ACCEPTED")
                        .build());

        ExchangeRequestResponse response = exchangeRequestService.updateRequest("req-1", updateDto);

        assertNotNull(response);
        assertEquals("ACCEPTED", response.getStatus());
        
        ArgumentCaptor<ExchangeRequest> captor = ArgumentCaptor.forClass(ExchangeRequest.class);
        verify(exchangeRequestRepository).save(captor.capture());
        assertEquals(ExchangeRequest.RequestStatus.ACCEPTED, captor.getValue().getStatus());
    }

    @Test
    void testUpdateRequest_shouldRejectRequest() {
        UpdateExchangeRequestDto updateDto = UpdateExchangeRequestDto.builder()
                .accepted(false)
                .build();

        when(exchangeRequestRepository.findById("req-1")).thenReturn(Optional.of(testRequest));
        when(exchangeRequestRepository.save(any(ExchangeRequest.class))).thenReturn(testRequest);
        when(userService.getUserById("user-1")).thenReturn(senderUser);
        when(userService.getUserById("user-2")).thenReturn(receiverUser);
        when(mapper.toExchangeRequestResponse(testRequest, senderUser, receiverUser))
                .thenReturn(ExchangeRequestResponse.builder()
                        .id("req-1")
                        .status("REJECTED")
                        .build());

        ExchangeRequestResponse response = exchangeRequestService.updateRequest("req-1", updateDto);

        assertNotNull(response);
        ArgumentCaptor<ExchangeRequest> captor = ArgumentCaptor.forClass(ExchangeRequest.class);
        verify(exchangeRequestRepository).save(captor.capture());
        assertEquals(ExchangeRequest.RequestStatus.REJECTED, captor.getValue().getStatus());
    }

    @Test
    void testUpdateRequest_shouldThrowExceptionWhenNotFound() {
        UpdateExchangeRequestDto updateDto = UpdateExchangeRequestDto.builder()
                .accepted(true)
                .build();

        when(exchangeRequestRepository.findById("non-existing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exchangeRequestService.updateRequest("non-existing", updateDto));
    }
}
