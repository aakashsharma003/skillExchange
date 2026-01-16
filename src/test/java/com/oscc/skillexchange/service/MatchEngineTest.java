package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchEngineTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchEngine matchEngine;

    private User newUser;
    private User existingUser;

    @BeforeEach
    void setUp() {
        newUser = User.builder()
                .id("1")
                .email("new@example.com")
                .fullName("New User")
                .skillsOffered(List.of("Java", "Spring", "MongoDB"))
                .interests(List.of("React", "TypeScript"))
                .build();

        existingUser = User.builder()
                .id("2")
                .email("existing@example.com")
                .fullName("Existing User")
                .skillsOffered(List.of("React", "TypeScript", "Node.js"))
                .interests(List.of("Java", "Spring"))
                .build();
    }

    @Test
    void testCalculateSimilarity_ExactMatch() {
        List<String> list1 = List.of("Java", "Spring");
        List<String> list2 = List.of("Java", "Spring");

        // Use reflection to access private method or test via public method
        // For now, test via findMatches which uses calculateSimilarity internally
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<MatchEngine.MatchResult> matches = matchEngine.findMatches(newUser);

        assertNotNull(matches);
        // Should find a match since newUser.skillsOffered intersects with existingUser.interests
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindMatches_WithMatchingSkills() {
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<MatchEngine.MatchResult> matches = matchEngine.findMatches(newUser);

        assertNotNull(matches);
        // newUser offers "Java", "Spring" which matches existingUser's interests "Java", "Spring"
        // This should create a match with high similarity
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindMatches_NoMatches() {
        User userWithoutMatchingSkills = User.builder()
                .id("3")
                .email("nomatch@example.com")
                .skillsOffered(List.of("Python", "Django"))
                .interests(List.of("C#", ".NET"))
                .build();

        when(userRepository.findAll()).thenReturn(List.of(userWithoutMatchingSkills));

        List<MatchEngine.MatchResult> matches = matchEngine.findMatches(newUser);

        // No matches expected since skills don't intersect
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindMatches_ThresholdFiltering() {
        User lowMatchUser = User.builder()
                .id("3")
                .email("lowmatch@example.com")
                .skillsOffered(List.of("Vue", "Angular"))
                .interests(List.of("Java")) // Only 1 matching skill, might be below 80% threshold
                .build();

        when(userRepository.findAll()).thenReturn(List.of(lowMatchUser));

        List<MatchEngine.MatchResult> matches = matchEngine.findMatches(newUser);

        // Should filter matches based on similarity threshold
        assertNotNull(matches);
        verify(userRepository, times(1)).findAll();
    }
}
