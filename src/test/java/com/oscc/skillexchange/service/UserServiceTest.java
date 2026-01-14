package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addSkill_shouldAddSkillWhenNotPresent() {
        String userId = "user-1";
        String skill = "Java";

        User user = User.builder()
                .id(userId)
                .skills(new ArrayList<>())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityMapper.toUserResponse(any(User.class))).thenReturn(UserResponse.builder().id(userId).build());

        UserResponse response = userService.addSkill(userId, skill);

        assertNotNull(response);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().getSkills().contains(skill));
    }

    @Test
    void addSkill_shouldNotDuplicateExistingSkill() {
        String userId = "user-1";
        String skill = "Java";

        List<String> skills = new ArrayList<>();
        skills.add(skill);

        User user = User.builder()
                .id(userId)
                .skills(skills)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityMapper.toUserResponse(any(User.class))).thenReturn(UserResponse.builder().id(userId).build());

        userService.addSkill(userId, skill);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getSkills().size());
    }

    @Test
    void removeSkill_shouldRemoveExistingSkill() {
        String userId = "user-1";
        String skill = "Java";

        List<String> skills = new ArrayList<>();
        skills.add(skill);

        User user = User.builder()
                .id(userId)
                .skills(skills)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityMapper.toUserResponse(any(User.class))).thenReturn(UserResponse.builder().id(userId).build());

        userService.removeSkill(userId, skill);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertFalse(captor.getValue().getSkills().contains(skill));
    }

    @Test
    void removeSkill_shouldHandleNonExistingUser() {
        String userId = "non-existing";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.removeSkill(userId, "Java"));
    }
}


