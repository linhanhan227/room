package com.chat.room.controller;

import com.chat.room.dto.AuthResponse;
import com.chat.room.dto.LoginRequest;
import com.chat.room.dto.RegisterRequest;
import com.chat.room.dto.UserDTO;
import com.chat.room.entity.User;
import com.chat.room.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .nickname("Test User")
                .email("test@example.com")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        UserDTO userDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .nickname("Test User")
                .email("test@example.com")
                .status(User.UserStatus.OFFLINE)
                .role(User.UserRole.USER)
                .build();

        authResponse = AuthResponse.builder()
                .token("jwt-token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userDTO)
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void register_Success() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));
    }

    @Test
    @DisplayName("Should login user successfully")
    void login_Success() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    @DisplayName("Should fail registration with invalid data")
    void register_InvalidData() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("ab")
                .password("123")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
