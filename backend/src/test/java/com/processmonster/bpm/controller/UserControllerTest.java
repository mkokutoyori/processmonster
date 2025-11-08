package com.processmonster.bpm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmonster.bpm.dto.user.CreateUserDTO;
import com.processmonster.bpm.dto.user.UpdateUserDTO;
import com.processmonster.bpm.dto.user.UserDTO;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO testUserDTO;
    private CreateUserDTO createUserDTO;
    private UpdateUserDTO updateUserDTO;

    @BeforeEach
    void setUp() {
        testUserDTO = UserDTO.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .enabled(true)
            .roles(Set.of("ROLE_USER"))
            .permissions(Set.of("USER_READ"))
            .build();

        createUserDTO = CreateUserDTO.builder()
            .username("newuser")
            .email("new@example.com")
            .password("Password123!")
            .firstName("New")
            .lastName("User")
            .enabled(true)
            .roleIds(Set.of(1L))
            .build();

        updateUserDTO = UpdateUserDTO.builder()
            .email("updated@example.com")
            .firstName("Updated")
            .build();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void getAllUsers_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"USER_READ"})
    @DisplayName("Should get all users when authenticated")
    void getAllUsers_ShouldReturnUsers_WhenAuthenticated() throws Exception {
        // Arrange
        Page<UserDTO> userPage = new PageImpl<>(List.of(testUserDTO));
        when(userService.getAllUsers(any())).thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].username").value("testuser"))
            .andExpect(jsonPath("$.content[0].email").value("test@example.com"));

        verify(userService).getAllUsers(any());
    }

    @Test
    @WithMockUser(authorities = {"USER_READ"})
    @DisplayName("Should search users successfully")
    void searchUsers_ShouldReturnMatchingUsers() throws Exception {
        // Arrange
        Page<UserDTO> userPage = new PageImpl<>(List.of(testUserDTO));
        when(userService.searchUsers(eq("test"), any())).thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search")
                .param("keyword", "test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].username").value("testuser"));

        verify(userService).searchUsers(eq("test"), any());
    }

    @Test
    @WithMockUser(authorities = {"USER_READ"})
    @DisplayName("Should get user by ID successfully")
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(authorities = {"USER_READ"})
    @DisplayName("Should return 404 when user not found")
    void getUserById_ShouldReturn404_WhenUserNotFound() throws Exception {
        // Arrange
        when(userService.getUserById(999L))
            .thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/999"))
            .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    @Test
    @WithMockUser(authorities = {"USER_WRITE"})
    @DisplayName("Should create user successfully")
    void createUser_ShouldReturn201_WhenDataIsValid() throws Exception {
        // Arrange
        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).createUser(any(CreateUserDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"USER_WRITE"})
    @DisplayName("Should return 400 when creating user with invalid data")
    void createUser_ShouldReturn400_WhenDataIsInvalid() throws Exception {
        // Arrange
        CreateUserDTO invalidDTO = CreateUserDTO.builder()
            .username("ab")  // Too short
            .email("invalid-email")  // Invalid email
            .password("weak")  // Weak password
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    @WithMockUser(authorities = {"USER_WRITE"})
    @DisplayName("Should return 400 when creating user with existing username")
    void createUser_ShouldReturn400_WhenUsernameExists() throws Exception {
        // Arrange
        when(userService.createUser(any(CreateUserDTO.class)))
            .thenThrow(new BusinessException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
            .andExpect(status().isBadRequest());

        verify(userService).createUser(any(CreateUserDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"USER_WRITE"})
    @DisplayName("Should update user successfully")
    void updateUser_ShouldReturn200_WhenDataIsValid() throws Exception {
        // Arrange
        UserDTO updatedUser = UserDTO.builder()
            .id(1L)
            .username("testuser")
            .email("updated@example.com")
            .firstName("Updated")
            .build();

        when(userService.updateUser(eq(1L), any(UpdateUserDTO.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("updated@example.com"))
            .andExpect(jsonPath("$.firstName").value("Updated"));

        verify(userService).updateUser(eq(1L), any(UpdateUserDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"USER_DELETE"})
    @DisplayName("Should delete user successfully")
    void deleteUser_ShouldReturn204_WhenUserExists() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1"))
            .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(authorities = {"USER_DELETE"})
    @DisplayName("Should return 400 when deleting yourself")
    void deleteUser_ShouldReturn400_WhenDeletingSelf() throws Exception {
        // Arrange
        doThrow(new BusinessException("Cannot delete yourself"))
            .when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1"))
            .andExpect(status().isBadRequest());

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(authorities = {"USER_WRITE"})
    @DisplayName("Should activate user successfully")
    void activateUser_ShouldReturn200() throws Exception {
        // Arrange
        testUserDTO.setEnabled(true);
        when(userService.activateUser(1L)).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1/activate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(true));

        verify(userService).activateUser(1L);
    }

    @Test
    @WithMockUser(authorities = {"USER_WRITE"})
    @DisplayName("Should deactivate user successfully")
    void deactivateUser_ShouldReturn200() throws Exception {
        // Arrange
        testUserDTO.setEnabled(false);
        when(userService.deactivateUser(1L)).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1/deactivate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(false));

        verify(userService).deactivateUser(1L);
    }

    @Test
    @WithMockUser(authorities = {"USER_WRITE"})
    @DisplayName("Should change password successfully")
    void changePassword_ShouldReturn200() throws Exception {
        // Arrange
        doNothing().when(userService).changePassword(eq(1L), anyString());

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"NewPassword123!\""))
            .andExpect(status().isOk());

        verify(userService).changePassword(eq(1L), anyString());
    }

    @Test
    @WithMockUser(authorities = {"USER_READ"})
    @DisplayName("Should return 403 when user lacks write permission")
    void createUser_ShouldReturn403_WhenLackingPermission() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
            .andExpect(status().isForbidden());

        verify(userService, never()).createUser(any());
    }

    @Test
    @WithMockUser(authorities = {"USER_READ"})
    @DisplayName("Should get users by role successfully")
    void getUsersByRole_ShouldReturnUsers() throws Exception {
        // Arrange
        Page<UserDTO> userPage = new PageImpl<>(List.of(testUserDTO));
        when(userService.getUsersByRole(eq("ROLE_USER"), any())).thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/by-role/ROLE_USER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].username").value("testuser"));

        verify(userService).getUsersByRole(eq("ROLE_USER"), any());
    }
}
