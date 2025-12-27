package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.ChatMessage;
import com.oscc.skillexchange.domain.entity.ChatRoom;
import com.oscc.skillexchange.domain.entity.ExchangeRequest;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.request.ExchangeRequestDto;
import com.oscc.skillexchange.dto.response.ExchangeRequestResponse;
import com.oscc.skillexchange.dto.request.UpdateExchangeRequestDto;
import com.oscc.skillexchange.exception.DuplicateResourceException;
import com.oscc.skillexchange.exception.ResourceNotFoundException;
import com.oscc.skillexchange.repository.ExchangeRequestRepository;
import com.oscc.skillexchange.util.AppConstants;
import com.oscc.skillexchange.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRequestService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserService userService;
    private final EntityMapper mapper;
    private final ChatService chatService;

    /**
     * Create exchange request
     */
    @Transactional
    public ExchangeRequestResponse createRequest(String senderId, ExchangeRequestDto dto) {
        String receiverEmail = dto.getReceiverId();
        log.info("Creating exchange request from sender ID {} to receiver email {}", senderId, receiverEmail);

        // 1. Find the receiver user by email to get their actual ID
        User receiver = userService.getUserByEmail(receiverEmail);
        if (receiver == null) {
            throw new ResourceNotFoundException("Receiver with email " + receiverEmail + " not found");
        }

        String actualReceiverId = receiver.getId();

        // 2. Check for duplicate using the actual ID
        exchangeRequestRepository.findBySenderAndReceiverAndSkillAndStatusIn(
                senderId,
                actualReceiverId,
                dto.getRequestedSkill(),
                List.of(ExchangeRequest.RequestStatus.PENDING, ExchangeRequest.RequestStatus.ACCEPTED)
        ).ifPresent(req -> {
            throw new DuplicateResourceException(AppConstants.Messages.DUPLICATE_REQUEST);
        });

        // 3. Map DTO to Entity
        ExchangeRequest request = mapper.toExchangeRequest(dto, senderId);

        // 4. Manually set the correct receiver ID (since DTO had the email)
        request.setReceiverId(actualReceiverId);

        request = exchangeRequestRepository.save(request);

        // 5. Get sender details for the response
        User sender = userService.getUserById(senderId);

        log.info("Exchange request created successfully: {}", request.getId());

        // 6. Return response with actual User objects
        return mapper.toExchangeRequestResponse(request, sender, receiver);
    }

    /**
     * Get all requests for user (sent + received)
     */
    public List<ExchangeRequestResponse> getAllRequestsForUser(String userEmail) {
        log.info("Fetching all requests for user email: {}", userEmail);

        // 1. Find the user by email to get their actual ID
        User user = userService.getUserByEmail(userEmail);
        if (user == null) {
            log.warn("User with email {} not found", userEmail);
            return Collections.emptyList();
        }

        // 2. Use the user's actual ID for the repository query
        // Fixed the variable name from 'Email' to 'user.getId()'
        List<ExchangeRequest> requests = exchangeRequestRepository.findByUserIdAndStatusIn(
                user.getId(),
                List.of(ExchangeRequest.RequestStatus.values())
        );

        return mapToResponses(requests);
    }

    /**
     * Get sent requests
     */
    public List<ExchangeRequestResponse> getSentRequests(String userId) {
        log.info("Fetching sent requests for user: {}", userId);

        List<ExchangeRequest> requests = exchangeRequestRepository
                .findBySenderIdOrderByCreatedAtDesc(userId);

        return mapToResponses(requests);
    }

    /**
     * Get received requests
     */
    public List<ExchangeRequestResponse> getReceivedRequests(String userId) {
        log.info("Fetching received requests for user: {}", userId);

        List<ExchangeRequest> requests = exchangeRequestRepository
                .findByReceiverIdOrderByCreatedAtDesc(userId);

        return mapToResponses(requests);
    }

    /**
     * Update request status
     */
    @Transactional
    public ExchangeRequestResponse updateRequest(String requestId, UpdateExchangeRequestDto dto) {
        log.info("Updating exchange request: {}", requestId);

        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request", requestId));

        if (dto.getAccepted()) {
            request.setStatus(ExchangeRequest.RequestStatus.ACCEPTED);
            if (dto.getOfferedSkill() != null) {
                request.setOfferedSkill(dto.getOfferedSkill());
            }

            // --- Chat Start Logic ---
            // 1. Pehle Chat Room create ya fetch karo
            ChatRoom room = chatService.createOrGetChatRoom(
                    request.getSenderId(),
                    request.getReceiverId(),
                    request.getId()
            );

            // 2. Initial message ko ChatMessage object mein convert karke save karo
            ChatMessage firstMessage = ChatMessage.builder()
                    .chatRoomId(room.getId())
                    .senderId(request.getSenderId()) // Jisne request bheji thi wahi sender hai
                    .content(request.getMessage())   // Request ka original message content
                    .createdAt(Instant.now())
                    .build();

            chatService.saveMessage(firstMessage);
            log.info("Chat initialized and first message sent for room: {}", room.getId());

        } else {
            request.setStatus(ExchangeRequest.RequestStatus.REJECTED);
        }

        request = exchangeRequestRepository.save(request);

        User sender = userService.getUserById(request.getSenderId());
        User receiver = userService.getUserById(request.getReceiverId());

        log.info("Exchange request updated successfully: {}", requestId);
        return mapper.toExchangeRequestResponse(request, sender, receiver);
    }

    // Helper method
    private List<ExchangeRequestResponse> mapToResponses(List<ExchangeRequest> requests) {
        return requests.stream()
                .map(req -> {
                    User sender = userService.getUserById(req.getSenderId());
                    User receiver = userService.getUserById(req.getReceiverId());
                    return mapper.toExchangeRequestResponse(req, sender, receiver);
                })
                .collect(Collectors.toList());
    }
}
