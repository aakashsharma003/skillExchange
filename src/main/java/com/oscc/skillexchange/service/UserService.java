package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.response.ApiResponse;
import com.oscc.skillexchange.dto.response.UserResponse;
import com.oscc.skillexchange.exception.ResourceNotFoundException;
import com.oscc.skillexchange.repository.UserRepository;
import com.oscc.skillexchange.util.AppConstants;
import com.oscc.skillexchange.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EntityMapper mapper;

    /**
     * Get user by ID
     */
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.Messages.USER_NOT_FOUND));
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateProfile(String userId, ApiResponse.UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        User user = getUserById(userId);
        mapper.updateUserFromDto(user, request);

        user = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", userId);

        return mapper.toUserResponse(user);
    }

    /**
     * Search users by skill
     */
    public List<UserResponse> searchUsersBySkill(String skill, String excludeEmail) {
        log.info("Searching users by skill: {}", skill);

        List<User> users = userRepository.findBySkillsContainingAndEmailNot(
                skill, excludeEmail.toLowerCase()
        );

        return users.stream()
                .map(mapper::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all users except current user
     */
    public List<UserResponse> getAllUsers(String excludeEmail) {
        log.info("Fetching all users");

        List<User> users = userRepository.findAllExcludingEmail(excludeEmail.toLowerCase());

        return users.stream()
                .map(mapper::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all distinct skills
     */
    public List<String> getAllDistinctSkills() {
        log.info("Fetching all distinct skills");

        return userRepository.findAllProjectedSkills().stream()
                .flatMap(user -> user.getSkills().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Add a skill to the user's skill list (if not already present)
     */
    @Transactional
    public UserResponse addSkill(String userId, String skill) {
        log.info("Adding skill '{}' for user: {}", skill, userId);

        User user = getUserById(userId);
        if (user.getSkills() == null) {
            user.setSkills(List.of(skill));
        } else if (!user.getSkills().contains(skill)) {
            user.getSkills().add(skill);
        }

        user = userRepository.save(user);
        return mapper.toUserResponse(user);
    }

    /**
     * Remove a skill from the user's skill list
     */
    @Transactional
    public UserResponse removeSkill(String userId, String skill) {
        log.info("Removing skill '{}' for user: {}", skill, userId);

        User user = getUserById(userId);
        if (user.getSkills() != null && user.getSkills().removeIf(s -> s.equalsIgnoreCase(skill))) {
            user = userRepository.save(user);
        }

        return mapper.toUserResponse(user);
    }
}
