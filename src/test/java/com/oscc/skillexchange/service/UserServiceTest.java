package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.response.ApiResponse;
import com.oscc.skillexchange.dto.response.UserResponse;
import com.oscc.skillexchange.exception.ResourceNotFoundException;
import com.oscc.skillexchange.repository.UserRepository;
import com.oscc.skillexchange.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private MatchEngine matchEngine;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = User.builder()
                .id("user-1")
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .skillsOffered(new ArrayList<>(List.of("Java", "Spring Boot")))
                .interests(new ArrayList<>(List.of("Python", "Docker")))
                .learningProgress(new HashMap<>())
                .enabled(true)
                .locked(false)
                .createdAt(Instant.now())
                .build();

        testUserResponse = UserResponse.builder()
                .id("user-1")
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .skillsOffered(List.of("Java", "Spring Boot"))
                .interests(List.of("Python", "Docker"))
                .build();
    }

    @Test
    void testGetUserById_shouldReturnUser() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

        User result = userService.getUserById("user-1");

        assertNotNull(result);
        assertEquals("user-1", result.getId());
        assertEquals("John Doe", result.getFullName());
        verify(userRepository).findById("user-1");
    }

    @Test
    void testGetUserById_shouldThrowExceptionWhenNotFound() {
        when(userRepository.findById("non-existing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById("non-existing"));
    }

    @Test
    void testGetUserByEmail_shouldReturnUser() {
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.getUserByEmail("john@example.com");

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).findByEmailIgnoreCase("john@example.com");
    }

    @Test
    void testGetUserByEmail_shouldThrowExceptionWhenNotFound() {
        when(userRepository.findByEmailIgnoreCase("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail("nonexistent@example.com"));
    }

    @Test
    void testGetUserResponse_shouldMapUserToResponse() {
        when(entityMapper.toUserResponse(testUser)).thenReturn(testUserResponse);

        UserResponse result = userService.getUserResponse(testUser);

        assertNotNull(result);
        assertEquals("user-1", result.getId());
        assertEquals("john@example.com", result.getEmail());
        verify(entityMapper).toUserResponse(testUser);
    }

    @Test
    void testUpdateProfile_shouldUpdateUserFields() {
        ApiResponse.UpdateProfileRequest request = ApiResponse.UpdateProfileRequest.builder()
                .fullName("Jane Doe")
                .bio("Updated bio")
                .location("New York")
                .skillsOffered(List.of("Python", "Java"))
                .interests(List.of("AI", "Docker"))
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
        when(entityMapper.toUserResponse(any(User.class))).thenReturn(testUserResponse);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(matchEngine.findMatches(any(User.class))).thenReturn(new ArrayList<>());

        UserResponse result = userService.updateProfile("user-1", request);

        assertNotNull(result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("Jane Doe", captor.getValue().getFullName());
        assertEquals("Updated bio", captor.getValue().getBio());
    }

    @Test
    void testSearchUsersBySkill_shouldReturnUsersWithSkill() {
        User user2 = User.builder()
                .id("user-2")
                .fullName("Jane Smith")
                .email("jane@example.com")
                .skillsOffered(List.of("Python"))
                .build();

        when(userRepository.findBySkillsOfferedContainingAndEmailNot("Java", "john@example.com"))
                .thenReturn(List.of(user2));
        when(entityMapper.toUserResponse(user2)).thenReturn(
                UserResponse.builder().id("user-2").fullName("Jane Smith").build()
        );

        List<UserResponse> results = userService.searchUsersBySkill("Java", "john@example.com");

        assertEquals(1, results.size());
        verify(userRepository).findBySkillsOfferedContainingAndEmailNot("Java", "john@example.com");
    }

    @Test
    void testSearchUsersBySkill_shouldReturnEmptyListWhenNoMatch() {
        when(userRepository.findBySkillsOfferedContainingAndEmailNot("Rust", "john@example.com"))
                .thenReturn(new ArrayList<>());

        List<UserResponse> results = userService.searchUsersBySkill("Rust", "john@example.com");

        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchUsersByInterest_shouldReturnUsersWithInterest() {
        User user2 = User.builder()
                .id("user-2")
                .email("jane@example.com")
                .interests(List.of("Python"))
                .build();

        when(userRepository.findByInterestsContainingAndEmailNot("Python", "john@example.com"))
                .thenReturn(List.of(user2));
        when(entityMapper.toUserResponse(user2)).thenReturn(
                UserResponse.builder().id("user-2").build()
        );

        List<UserResponse> results = userService.searchUsersByInterest("Python", "john@example.com");

        assertEquals(1, results.size());
    }

    @Test
    void testGetAllUsers_shouldReturnAllUsersExceptCurrent() {
        User user2 = User.builder().id("user-2").email("jane@example.com").build();
        User user3 = User.builder().id("user-3").email("bob@example.com").build();

        when(userRepository.findAllExcludingEmail("john@example.com"))
                .thenReturn(List.of(user2, user3));
        when(entityMapper.toUserResponse(any())).thenReturn(testUserResponse);

        List<UserResponse> results = userService.getAllUsers("john@example.com");

        assertEquals(2, results.size());
        verify(userRepository).findAllExcludingEmail("john@example.com");
    }

    @Test
    void testGetAllDistinctSkills_shouldReturnSortedUniqueSkills() {
        User user1 = User.builder()
                .skillsOffered(List.of("Java", "Python"))
                .interests(List.of("Docker"))
                .build();
        User user2 = User.builder()
                .skillsOffered(List.of("Python"))
                .interests(List.of("Kubernetes", "Java"))
                .build();

        when(userRepository.findAllProjectedSkills()).thenReturn(List.of(user1, user2));

        List<String> results = userService.getAllDistinctSkills();

        assertTrue(results.contains("Java"));
        assertTrue(results.contains("Python"));
        assertTrue(results.contains("Docker"));
        assertEquals(results, results.stream().sorted().toList());
    }
}


