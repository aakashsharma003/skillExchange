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
    private final MatchEngine matchEngine;
    private final NotificationService notificationService;

    /**
     * Get UserResponse from User entity
     */
    public UserResponse getUserResponse(User user) {
        return mapper.toUserResponse(user);
    }

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

        // Trigger match checking after profile update
        checkAndNotifyMatches(user);

        return mapper.toUserResponse(user);
    }

    /**
     * Check for matches and send notifications
     */
    private void checkAndNotifyMatches(User user) {
        try {
            List<MatchEngine.MatchResult> matches = matchEngine.findMatches(user);
            for (MatchEngine.MatchResult match : matches) {
                User matchedUser = match.getUser();
                if (!match.getMatchingSkills().isEmpty()) {
                    String skill = match.getMatchingSkills().get(0);
                    notificationService.sendMatchNotification(
                            matchedUser.getId(),
                            user.getFullName(),
                            skill
                    );
                }
            }
        } catch (Exception e) {
            log.error("Error checking matches for user: {}", user.getId(), e);
        }
    }

    /**
     * Search users by skillsOffered (what they can teach)
     */
    public List<UserResponse> searchUsersBySkill(String skill, String excludeEmail) {
        log.info("Searching users by skill offered: {}", skill);

        List<User> users = userRepository.findBySkillsOfferedContainingAndEmailNot(
                skill, excludeEmail.toLowerCase()
        );

        return users.stream()
                .map(mapper::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search users by interest (what they want to learn)
     */
    public List<UserResponse> searchUsersByInterest(String interest, String excludeEmail) {
        log.info("Searching users by interest: {}", interest);

        List<User> users = userRepository.findByInterestsContainingAndEmailNot(
                interest, excludeEmail.toLowerCase()
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
     * Get all distinct skills (from skillsOffered and interests)
     */
    public List<String> getAllDistinctSkills() {
        log.info("Fetching all distinct skills");

        return userRepository.findAllProjectedSkills().stream()
                .flatMap(user -> {
                    List<String> all = new java.util.ArrayList<>();
                    if (user.getSkillsOffered() != null) all.addAll(user.getSkillsOffered());
                    if (user.getInterests() != null) all.addAll(user.getInterests());
                    return all.stream();
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Delete user account permanently
     */
    @Transactional
    public void deleteUserAccount(String userId) {
        log.info("Deleting user account: {}", userId);

        User user = getUserById(userId);
        userRepository.delete(user);

        log.info("User account deleted successfully: {}", userId);
    }

    /**
     * Report suspicious activity
     */
    @Transactional
    public void reportSuspiciousActivity(String userId, String description, String relatedUser) {
        log.info("Recording suspicious activity report from user: {} - Related user: {}", userId, relatedUser);

        User user = getUserById(userId);
        
        // Log the report for admin review
        // In a real scenario, you would save this to a database collection/table
        log.warn("SUSPICIOUS ACTIVITY REPORT - Reporter: {}, Related User: {}, Description: {}", 
                user.getEmail(), relatedUser, description);

        // You can also store in a separate ActivityReport entity if needed
        // This is a simple logging approach for now
    }
}
