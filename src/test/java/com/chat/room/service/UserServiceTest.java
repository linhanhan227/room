package com.chat.room.service;

import com.chat.room.dto.AuthResponse;
import com.chat.room.dto.LoginRequest;
import com.chat.room.dto.RegisterRequest;
import com.chat.room.dto.UserDTO;
import com.chat.room.entity.User;
import com.chat.room.exception.BusinessException;
import com.chat.room.repository.BannedUserRepository;
import com.chat.room.repository.UserRepository;
import com.chat.room.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BannedUserRepository bannedUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .nickname("Test User")
                .email("test@example.com")
                .status(User.UserStatus.OFFLINE)
                .role(User.UserRole.USER)
                .build();

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
    }

    @Test
    @DisplayName("Should register user successfully")
    void register_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenProvider.generateToken(anyString())).thenReturn("jwt-token");
        when(tokenProvider.getExpiration()).thenReturn(86400000L);

        AuthResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("testuser", response.getUser().getUsername());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void register_UsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void register_EmailExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login user successfully")
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateToken("testuser")).thenReturn("jwt-token");
        when(tokenProvider.getExpiration()).thenReturn(86400000L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(User.UserStatus.ONLINE, testUser.getStatus());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by id")
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("Should update user status")
    void updateUserStatus_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.updateUserStatus(1L, User.UserStatus.ONLINE);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }
}
