package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.response.UserResponse;
import com.oscc.skillexchange.exception.ResourceNotFoundException;
import com.oscc.skillexchange.repository.UserRepository;
import com.oscc.skillexchange.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper mapper;

    @Mock
    private MatchEngine matchEngine;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("1")
                .email("test@example.com")
                .fullName("Test User")
                .skillsOffered(List.of("Java", "Spring"))
                .interests(List.of("React", "TypeScript"))
                .build();
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));

        User result = userService.getUserById("1");

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findById("1");
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById("1"));
        verify(userRepository, times(1)).findById("1");
    }

    @Test
    void testSearchUsersBySkill() {
        User matchingUser = User.builder()
                .id("2")
                .email("other@example.com")
                .fullName("Other User")
                .skillsOffered(List.of("Java", "Python"))
                .interests(new ArrayList<>())
                .build();

        when(userRepository.findBySkillsOfferedContainingAndEmailNot("Java", "test@example.com"))
                .thenReturn(List.of(matchingUser));

        UserResponse response = UserResponse.builder()
                .id("2")
                .email("other@example.com")
                .fullName("Other User")
                .skillsOffered(List.of("Java", "Python"))
                .interests(new ArrayList<>())
                .build();

        when(mapper.toUserResponse(matchingUser)).thenReturn(response);

        List<UserResponse> results = userService.searchUsersBySkill("Java", "test@example.com");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("2", results.get(0).getId());
        verify(userRepository, times(1)).findBySkillsOfferedContainingAndEmailNot("Java", "test@example.com");
    }

    @Test
    void testSearchUsersByInterest() {
        User matchingUser = User.builder()
                .id("2")
                .email("other@example.com")
                .fullName("Other User")
                .skillsOffered(new ArrayList<>())
                .interests(List.of("Java", "Python"))
                .build();

        when(userRepository.findByInterestsContainingAndEmailNot("Java", "test@example.com"))
                .thenReturn(List.of(matchingUser));

        UserResponse response = UserResponse.builder()
                .id("2")
                .email("other@example.com")
                .fullName("Other User")
                .skillsOffered(new ArrayList<>())
                .interests(List.of("Java", "Python"))
                .build();

        when(mapper.toUserResponse(matchingUser)).thenReturn(response);

        List<UserResponse> results = userService.searchUsersByInterest("Java", "test@example.com");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("2", results.get(0).getId());
        verify(userRepository, times(1)).findByInterestsContainingAndEmailNot("Java", "test@example.com");
    }

    @Test
    void testAddSkillOffered() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = UserResponse.builder()
                .id("1")
                .email("test@example.com")
                .fullName("Test User")
                .skillsOffered(List.of("Java", "Spring", "Python"))
                .build();

        when(mapper.toUserResponse(any(User.class))).thenReturn(response);

        UserResponse result = userService.addSkillOffered("1", "Python");

        assertNotNull(result);
        assertTrue(result.getSkillsOffered().contains("Python"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testAddInterest() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = UserResponse.builder()
                .id("1")
                .email("test@example.com")
                .fullName("Test User")
                .interests(List.of("React", "TypeScript", "Vue"))
                .build();

        when(mapper.toUserResponse(any(User.class))).thenReturn(response);

        UserResponse result = userService.addInterest("1", "Vue");

        assertNotNull(result);
        assertTrue(result.getInterests().contains("Vue"));
        verify(userRepository, times(1)).save(any(User.class));
    }
}
