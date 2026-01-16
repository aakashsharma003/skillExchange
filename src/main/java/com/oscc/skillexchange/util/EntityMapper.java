package com.oscc.skillexchange.util;

import com.oscc.skillexchange.domain.entity.*;
import com.oscc.skillexchange.dto.request.ExchangeRequestDto;
import com.oscc.skillexchange.dto.request.SignupRequest;
import com.oscc.skillexchange.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    // User mappings
    public UserResponse toUserResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .skillsOffered(user.getSkillsOffered())
                .interests(user.getInterests())
                .learningProgress(user.getLearningProgress())
                .githubProfile(user.getGithubProfile())
                .linkedinProfile(user.getLinkedinProfile())
                .youtubeProfile(user.getYoutubeProfile())
                .instagramProfile(user.getInstagramProfile())
                .bio(user.getBio())
                .location(user.getLocation())
                .profilePictureUrl(user.getProfilePictureUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User toUser(SignupRequest request) {
        if (request == null) return null;

        return User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .phone(request.getPhone())
                .skillsOffered(request.getSkillsOffered() != null && !request.getSkillsOffered().isEmpty() 
                        ? request.getSkillsOffered() : new ArrayList<>())
                .interests(request.getInterests() != null && !request.getInterests().isEmpty() 
                        ? request.getInterests() : new ArrayList<>())
                .githubProfile(request.getGithubProfile())
                .linkedinProfile(request.getLinkedinProfile())
                .youtubeProfile(request.getYoutubeProfile())
                .instagramProfile(request.getInstagramProfile())
                .bio(request.getBio())
                .location(request.getLocation())
                .profilePictureUrl(request.getProfilePictureUrl())
                .enabled(true)
                .locked(false)
                .build();
    }

    public void updateUserFromDto(User user, ApiResponse.UpdateProfileRequest request) {
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getSkillsOffered() != null) user.setSkillsOffered(request.getSkillsOffered());
        if (request.getInterests() != null) user.setInterests(request.getInterests());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getGithubProfile() != null) user.setGithubProfile(request.getGithubProfile());
        if (request.getLinkedinProfile() != null) user.setLinkedinProfile(request.getLinkedinProfile());
        if (request.getYoutubeProfile() != null) user.setYoutubeProfile(request.getYoutubeProfile());
        if (request.getInstagramProfile() != null) user.setInstagramProfile(request.getInstagramProfile());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getProfilePictureUrl() != null) user.setProfilePictureUrl(request.getProfilePictureUrl());
    }

    // ExchangeRequest mappings
    public ExchangeRequestResponse toExchangeRequestResponse(
            ExchangeRequest request, User sender, User receiver) {
        if (request == null) return null;

        return ExchangeRequestResponse.builder()
                .id(request.getId())
                .requestedSkill(request.getRequestedSkill())
                .offeredSkill(request.getOfferedSkill())
                .message(request.getMessage())
                .status(request.getStatus().name())
                .sender(toUserResponse(sender))
                .receiver(toUserResponse(receiver))
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    public ExchangeRequest toExchangeRequest(ExchangeRequestDto dto, String senderId) {
        if (dto == null) return null;

        return ExchangeRequest.builder()
                .senderId(senderId)
                .receiverId(dto.getReceiverId())
                .requestedSkill(dto.getRequestedSkill())
                .offeredSkill(dto.getOfferedSkill())
                .message(dto.getMessage())
                .status(ExchangeRequest.RequestStatus.PENDING)
                .build();
    }

    // ChatRoom mappings
    public ChatRoomResponse toChatRoomResponse(ChatRoom room, User otherUser) {
        if (room == null) return null;

        return ChatRoomResponse.builder()
                .id(room.getId())
                .chatRoomId(room.getId()) // Set both id and chatRoomId for frontend compatibility
                .otherUser(toUserResponse(otherUser))
                .exchangeRequestId(room.getExchangeRequestId())
                .lastActivityAt(room.getLastActivityAt())
                .build();
    }

    // ChatMessage mappings
    public ChatMessageResponse toChatMessageResponse(ChatMessage message) {
        if (message == null) return null;

        return ChatMessageResponse.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoomId())
                .senderEmail(message.getSenderEmail())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

    public List<ChatMessageResponse> toChatMessageResponseList(List<ChatMessage> messages) {
        if (messages == null) return List.of();
        return messages.stream()
                .map(this::toChatMessageResponse)
                .collect(Collectors.toList());
    }
}
