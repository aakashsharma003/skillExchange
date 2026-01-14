package com.oscc.skillexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.request.SkillUpdateRequest;
import com.oscc.skillexchange.dto.response.ApiResponse;
import com.oscc.skillexchange.dto.response.UserResponse;
import com.oscc.skillexchange.service.AuthService;
import com.oscc.skillexchange.service.UserService;
import com.oscc.skillexchange.util.AppConstants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    private static final String AUTH_HEADER_VALUE = "Bearer test-token";

    @Test
    void testGetProfile_shouldReturnUserProfile() throws Exception {
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .fullName("Test User")
                .build();

        Mockito.when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        Mockito.when(userService.getUserById("user-1")).thenReturn(user);
        Mockito.when(userService.getUserResponse(user))
                .thenReturn(UserResponse.builder()
                        .id("user-1")
                        .email("test@example.com")
                        .fullName("Test User")
                        .build());

        mockMvc.perform(get("/api/users/profile")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("user-1"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void testGetProfile_shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateProfile_shouldUpdateUserProfile() throws Exception {
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .build();

        ApiResponse.UpdateProfileRequest request = ApiResponse.UpdateProfileRequest.builder()
                .fullName("Updated Name")
                .bio("Updated bio")
                .skillsOffered(List.of("Java"))
                .interests(List.of("Python"))
                .build();

        UserResponse response = UserResponse.builder()
                .id("user-1")
                .fullName("Updated Name")
                .build();

        Mockito.when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        Mockito.when(userService.updateProfile(eq("user-1"), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/users/profile")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"));
    }

    @Test
    void testGetUserByEmail_shouldReturnUser() throws Exception {
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .fullName("Test User")
                .build();

        UserResponse response = UserResponse.builder()
                .id("user-1")
                .email("test@example.com")
                .fullName("Test User")
                .build();

        Mockito.when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        Mockito.when(userService.getUserResponse(user)).thenReturn(response);

        mockMvc.perform(get("/api/users")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void testSearchUsers_shouldReturnUsersWithSkill() throws Exception {
        User currentUser = User.builder()
                .id("user-1")
                .email("current@example.com")
                .build();

        UserResponse response = UserResponse.builder()
                .id("user-2")
                .email("other@example.com")
                .skillsOffered(List.of("Java"))
                .build();

        Mockito.when(authService.validateTokenAndGetUser(any())).thenReturn(currentUser);
        Mockito.when(userService.searchUsersBySkill("Java", "current@example.com"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/users/search")
                .param("skill", "Java")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].skillsOffered[0]").value("Java"));
    }

    @Test
    void testSearchUsers_shouldReturnUsersWithInterest() throws Exception {
        User currentUser = User.builder()
                .id("user-1")
                .email("current@example.com")
                .build();

        UserResponse response = UserResponse.builder()
                .id("user-2")
                .email("other@example.com")
                .interests(List.of("Python"))
                .build();

        Mockito.when(authService.validateTokenAndGetUser(any())).thenReturn(currentUser);
        Mockito.when(userService.searchUsersByInterest("Python", "current@example.com"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/users/search")
                .param("interest", "Python")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSearchUsers_shouldReturnAllUsersWhenNoFilter() throws Exception {
        User currentUser = User.builder()
                .id("user-1")
                .email("current@example.com")
                .build();

        UserResponse response1 = UserResponse.builder()
                .id("user-2")
                .email("user2@example.com")
                .build();

        UserResponse response2 = UserResponse.builder()
                .id("user-3")
                .email("user3@example.com")
                .build();

        Mockito.when(authService.validateTokenAndGetUser(any())).thenReturn(currentUser);
        Mockito.when(userService.getAllUsers("current@example.com"))
                .thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/users/search")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetAllSkills_shouldReturnDistinctSkills() throws Exception {
        List<String> skills = List.of("Java", "Python", "Docker", "Kubernetes");

        Mockito.when(userService.getAllDistinctSkills()).thenReturn(skills);

        mockMvc.perform(get("/api/users/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0]").value("Java"));
    }
}



