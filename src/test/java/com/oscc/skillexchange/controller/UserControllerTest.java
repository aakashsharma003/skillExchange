package com.oscc.skillexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.request.SkillUpdateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void addSkill_shouldReturnUpdatedUser() throws Exception {
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .build();

        Mockito.when(authService.validateTokenAndGetUser(any())).thenReturn(user);

        UserResponse userResponse = UserResponse.builder()
                .id("user-1")
                .email("test@example.com")
                .skills(List.of("Java"))
                .build();

        Mockito.when(userService.addSkill(eq("user-1"), eq("Java")))
                .thenReturn(userResponse);

        SkillUpdateRequest request = SkillUpdateRequest.builder()
                .skill("Java")
                .build();

        mockMvc.perform(post("/api/users/skills")
                        .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.skills[0]").value("Java"));
    }

    @Test
    void removeSkill_shouldReturnUpdatedUser() throws Exception {
        User user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .build();

        Mockito.when(authService.validateTokenAndGetUser(any())).thenReturn(user);

        UserResponse userResponse = UserResponse.builder()
                .id("user-1")
                .email("test@example.com")
                .skills(List.of())
                .build();

        Mockito.when(userService.removeSkill(eq("user-1"), eq("Java")))
                .thenReturn(userResponse);

        SkillUpdateRequest request = SkillUpdateRequest.builder()
                .skill("Java")
                .build();

        mockMvc.perform(delete("/api/users/skills")
                        .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.skills").isArray());
    }
}


