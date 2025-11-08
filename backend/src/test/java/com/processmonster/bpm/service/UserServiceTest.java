package com.processmonster.bpm.service;

import com.processmonster.bpm.dto.user.CreateUserDTO;
import com.processmonster.bpm.dto.user.UpdateUserDTO;
import com.processmonster.bpm.dto.user.UserDTO;
import com.processmonster.bpm.entity.Role;
import com.processmonster.bpm.entity.User;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.mapper.UserMapper;
import com.processmonster.bpm.repository.RoleRepository;
import com.processmonster.bpm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private CreateUserDTO createUserDTO;
    private UpdateUserDTO updateUserDTO;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Mock SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");

        // Mock MessageSource
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
            .thenReturn("Test message");

        // Setup test data
        testRole = Role.builder()
            .id(1L)
            .name("ROLE_USER")
            .permissions(new HashSet<>())
            .build();

        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .password("hashedPassword")
            .firstName("Test")
            .lastName("User")
            .enabled(true)
            .deleted(false)
            .roles(Set.of(testRole))
            .createdAt(LocalDateTime.now())
            .build();

        testUserDTO = UserDTO.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .enabled(true)
            .roles(Set.of("ROLE_USER"))
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
    @DisplayName("Should get all users successfully")
    void getAllUsers_ShouldReturnPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findByDeletedFalse(pageable)).thenReturn(userPage);
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        // Act
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");

        verify(userRepository).findByDeletedFalse(pageable);
        verify(userMapper).toDTO(testUser);
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findById(1L);
        verify(userMapper).toDTO(testUser);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(EntityNotFoundException.class);

        verify(userRepository).findById(999L);
        verify(userMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Should throw exception when user is deleted")
    void getUserById_ShouldThrowException_WhenUserIsDeleted() {
        // Arrange
        testUser.setDeleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(1L))
            .isInstanceOf(EntityNotFoundException.class);

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should search users by keyword successfully")
    void searchUsers_ShouldReturnMatchingUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.searchUsers("test", pageable)).thenReturn(userPage);
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        // Act
        Page<UserDTO> result = userService.searchUsers("test", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(userRepository).searchUsers("test", pageable);
    }

    @Test
    @DisplayName("Should create user successfully")
    void createUser_ShouldCreateUser_WhenDataIsValid() {
        // Arrange
        User newUser = User.builder()
            .username("newuser")
            .email("new@example.com")
            .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userMapper.toEntity(createUserDTO)).thenReturn(newUser);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");
        when(roleRepository.findAllById(Set.of(1L))).thenReturn(List.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.toDTO(newUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.createUser(createUserDTO);

        // Assert
        assertThat(result).isNotNull();

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void createUser_ShouldThrowException_WhenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserDTO))
            .isInstanceOf(BusinessException.class);

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void createUser_ShouldThrowException_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserDTO))
            .isInstanceOf(BusinessException.class);

        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_ShouldUpdateUser_WhenDataIsValid() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(roleRepository.findAllById(anySet())).thenReturn(List.of(testRole));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.updateUser(1L, updateUserDTO);

        // Assert
        assertThat(result).isNotNull();

        verify(userRepository).findById(1L);
        verify(userMapper).updateEntity(updateUserDTO, testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when updating to existing email")
    void updateUser_ShouldThrowException_WhenEmailExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(1L, updateUserDTO))
            .isInstanceOf(BusinessException.class);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete user successfully (soft delete)")
    void deleteUser_ShouldSoftDeleteUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        userService.deleteUser(1L);

        // Assert
        assertThat(testUser.getDeleted()).isTrue();
        assertThat(testUser.getEnabled()).isFalse();
        assertThat(testUser.getDeletedAt()).isNotNull();

        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should not allow user to delete themselves")
    void deleteUser_ShouldThrowException_WhenDeletingSelf() {
        // Arrange
        testUser.setUsername("admin");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(1L))
            .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should activate user successfully")
    void activateUser_ShouldEnableUser() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.activateUser(1L);

        // Assert
        assertThat(testUser.getEnabled()).isTrue();
        assertThat(result).isNotNull();

        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void deactivateUser_ShouldDisableUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.deactivateUser(1L);

        // Assert
        assertThat(testUser.getEnabled()).isFalse();
        assertThat(result).isNotNull();

        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should not allow user to deactivate themselves")
    void deactivateUser_ShouldThrowException_WhenDeactivatingSelf() {
        // Arrange
        testUser.setUsername("admin");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.deactivateUser(1L))
            .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should change password successfully")
    void changePassword_ShouldHashAndSaveNewPassword() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("newHashedPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        userService.changePassword(1L, "NewPassword123!");

        // Assert
        assertThat(testUser.getPassword()).isEqualTo("newHashedPassword");

        verify(passwordEncoder).encode("NewPassword123!");
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should get users by role successfully")
    void getUsersByRole_ShouldReturnUsersWithSpecificRole() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findByRoleName("ROLE_USER", pageable)).thenReturn(userPage);
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        // Act
        Page<UserDTO> result = userService.getUsersByRole("ROLE_USER", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(userRepository).findByRoleName("ROLE_USER", pageable);
    }
}
