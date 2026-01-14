package com.oscc.skillexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.request.ExchangeRequestDto;
import com.oscc.skillexchange.dto.request.UpdateExchangeRequestDto;
import com.oscc.skillexchange.dto.response.ApiResponse;
import com.oscc.skillexchange.dto.response.ExchangeRequestResponse;
import com.oscc.skillexchange.dto.response.UserResponse;
import com.oscc.skillexchange.service.AuthService;
import com.oscc.skillexchange.service.ExchangeRequestService;
import com.oscc.skillexchange.util.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExchangeRequestController.class)
class ExchangeRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExchangeRequestService exchangeRequestService;

    @MockBean
    private AuthService authService;

    private static final String AUTH_HEADER_VALUE = "Bearer test-token";

    @Test
    void testCreateRequest_shouldReturnCreatedRequest() throws Exception {
        User user = User.builder()
                .id("user-1")
                .email("john@example.com")
                .build();

        ExchangeRequestDto request = ExchangeRequestDto.builder()
                .receiverId("jane@example.com")
                .requestedSkill("Python")
                .offeredSkill("Java")
                .message("I want to learn Python")
                .build();

        ExchangeRequestResponse response = ExchangeRequestResponse.builder()
                .id("req-1")
                .requestedSkill("Python")
                .offeredSkill("Java")
                .status("PENDING")
                .sender(UserResponse.builder().id("user-1").build())
                .receiver(UserResponse.builder().id("user-2").build())
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(exchangeRequestService.createRequest(eq("user-1"), any())).thenReturn(response);

        mockMvc.perform(post("/api/exchange-requests")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Request created successfully"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void testCreateRequest_shouldReturnBadRequestWithMissingFields() throws Exception {
        ExchangeRequestDto request = ExchangeRequestDto.builder()
                .requestedSkill("Python")
                .build();

        User user = User.builder()
                .id("user-1")
                .email("john@example.com")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);

        mockMvc.perform(post("/api/exchange-requests")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllRequests_shouldReturnSentAndReceivedRequests() throws Exception {
        User user = User.builder()
                .id("user-1")
                .email("john@example.com")
                .build();

        ExchangeRequestResponse req1 = ExchangeRequestResponse.builder()
                .id("req-1")
                .status("PENDING")
                .build();

        ExchangeRequestResponse req2 = ExchangeRequestResponse.builder()
                .id("req-2")
                .status("ACCEPTED")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(exchangeRequestService.getAllRequestsForUser("john@example.com"))
                .thenReturn(List.of(req1, req2));

        mockMvc.perform(get("/api/exchange-requests")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetSentRequests_shouldReturnRequestsSentByUser() throws Exception {
        User user = User.builder()
                .id("user-1")
                .email("john@example.com")
                .build();

        ExchangeRequestResponse req = ExchangeRequestResponse.builder()
                .id("req-1")
                .status("PENDING")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(exchangeRequestService.getSentRequests("user-1"))
                .thenReturn(List.of(req));

        mockMvc.perform(get("/api/exchange-requests/sent")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("req-1"));
    }

    @Test
    void testGetReceivedRequests_shouldReturnRequestsReceivedByUser() throws Exception {
        User user = User.builder()
                .id("user-2")
                .email("jane@example.com")
                .build();

        ExchangeRequestResponse req = ExchangeRequestResponse.builder()
                .id("req-1")
                .status("PENDING")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(exchangeRequestService.getReceivedRequests("user-2"))
                .thenReturn(List.of(req));

        mockMvc.perform(get("/api/exchange-requests/received")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("req-1"));
    }

    @Test
    void testUpdateRequest_shouldAcceptRequest() throws Exception {
        User user = User.builder()
                .id("user-2")
                .email("jane@example.com")
                .build();

        UpdateExchangeRequestDto request = UpdateExchangeRequestDto.builder()
                .accepted(true)
                .offeredSkill("Python")
                .build();

        ExchangeRequestResponse response = ExchangeRequestResponse.builder()
                .id("req-1")
                .status("ACCEPTED")
                .offeredSkill("Python")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(exchangeRequestService.updateRequest(eq("req-1"), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/exchange-requests/req-1")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Request updated successfully"))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    void testUpdateRequest_shouldRejectRequest() throws Exception {
        User user = User.builder()
                .id("user-2")
                .email("jane@example.com")
                .build();

        UpdateExchangeRequestDto request = UpdateExchangeRequestDto.builder()
                .accepted(false)
                .build();

        ExchangeRequestResponse response = ExchangeRequestResponse.builder()
                .id("req-1")
                .status("REJECTED")
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(exchangeRequestService.updateRequest(eq("req-1"), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/exchange-requests/req-1")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    void testUpdateRequest_shouldReturnNotFoundWhenRequestDoesntExist() throws Exception {
        User user = User.builder()
                .id("user-2")
                .build();

        UpdateExchangeRequestDto request = UpdateExchangeRequestDto.builder()
                .accepted(true)
                .build();

        when(authService.validateTokenAndGetUser(any())).thenReturn(user);
        when(exchangeRequestService.updateRequest(eq("non-existing"), any()))
                .thenThrow(new com.oscc.skillexchange.exception.ResourceNotFoundException("Exchange request", "non-existing"));

        mockMvc.perform(put("/api/exchange-requests/non-existing")
                .header(AppConstants.AUTHORIZATION_HEADER, AUTH_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
