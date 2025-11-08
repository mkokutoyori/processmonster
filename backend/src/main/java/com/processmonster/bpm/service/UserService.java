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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Service
 *
 * Business logic for user management.
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    /**
     * Get all users (non-deleted, paginated)
     */
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findByDeletedFalse(pageable)
            .map(userMapper::toDTO);
    }

    /**
     * Get user by ID
     */
    public UserDTO getUserById(Long id) {
        User user = findUserById(id);
        return userMapper.toDTO(user);
    }

    /**
     * Search users by keyword
     */
    public Page<UserDTO> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable)
            .map(userMapper::toDTO);
    }

    /**
     * Get users by role
     */
    public Page<UserDTO> getUsersByRole(String roleName, Pageable pageable) {
        return userRepository.findByRoleName(roleName, pageable)
            .map(userMapper::toDTO);
    }

    /**
     * Create new user
     */
    @Transactional
    public UserDTO createUser(CreateUserDTO createDTO) {
        // Check if username already exists
        if (userRepository.existsByUsername(createDTO.getUsername())) {
            throw new BusinessException(getMessage("validation.unique", "Username"));
        }

        // Check if email already exists
        if (userRepository.existsByEmail(createDTO.getEmail())) {
            throw new BusinessException(getMessage("validation.unique", "Email"));
        }

        // Create user entity
        User user = userMapper.toEntity(createDTO);

        // Hash password
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));

        // Set roles
        if (createDTO.getRoleIds() != null && !createDTO.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(createDTO.getRoleIds()));
            user.setRoles(roles);
        }

        // Set audit fields
        String currentUser = getCurrentUsername();
        user.setCreatedBy(currentUser);
        user.setUpdatedBy(currentUser);

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created: {} by {}", savedUser.getUsername(), currentUser);

        return userMapper.toDTO(savedUser);
    }

    /**
     * Update existing user
     */
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserDTO updateDTO) {
        User user = findUserById(id);

        // Check if email is being changed and if it's already taken
        if (updateDTO.getEmail() != null &&
            !updateDTO.getEmail().equals(user.getEmail()) &&
            userRepository.existsByEmail(updateDTO.getEmail())) {
            throw new BusinessException(getMessage("validation.unique", "Email"));
        }

        // Update user fields
        userMapper.updateEntity(updateDTO, user);

        // Update roles if provided
        if (updateDTO.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(updateDTO.getRoleIds()));
            user.setRoles(roles);
        }

        // Set audit field
        user.setUpdatedBy(getCurrentUsername());

        // Save user
        User updatedUser = userRepository.save(user);
        log.info("User updated: {} by {}", updatedUser.getUsername(), getCurrentUsername());

        return userMapper.toDTO(updatedUser);
    }

    /**
     * Delete user (soft delete)
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);

        // Prevent deleting yourself
        if (user.getUsername().equals(getCurrentUsername())) {
            throw new BusinessException(getMessage("user.delete.self"));
        }

        // Soft delete
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setEnabled(false);

        userRepository.save(user);
        log.info("User deleted: {} by {}", user.getUsername(), getCurrentUsername());
    }

    /**
     * Activate user
     */
    @Transactional
    public UserDTO activateUser(Long id) {
        User user = findUserById(id);
        user.setEnabled(true);
        user.setUpdatedBy(getCurrentUsername());

        User savedUser = userRepository.save(user);
        log.info("User activated: {} by {}", savedUser.getUsername(), getCurrentUsername());

        return userMapper.toDTO(savedUser);
    }

    /**
     * Deactivate user
     */
    @Transactional
    public UserDTO deactivateUser(Long id) {
        User user = findUserById(id);

        // Prevent deactivating yourself
        if (user.getUsername().equals(getCurrentUsername())) {
            throw new BusinessException(getMessage("user.deactivate.self"));
        }

        user.setEnabled(false);
        user.setUpdatedBy(getCurrentUsername());

        User savedUser = userRepository.save(user);
        log.info("User deactivated: {} by {}", savedUser.getUsername(), getCurrentUsername());

        return userMapper.toDTO(savedUser);
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long id, String newPassword) {
        User user = findUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(getCurrentUsername());

        userRepository.save(user);
        log.info("Password changed for user: {} by {}", user.getUsername(), getCurrentUsername());
    }

    /**
     * Find user by ID or throw exception
     */
    private User findUserById(Long id) {
        return userRepository.findById(id)
            .filter(user -> !user.getDeleted())
            .orElseThrow(() -> new EntityNotFoundException(getMessage("user.notfound")));
    }

    /**
     * Get current authenticated username
     */
    private String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    /**
     * Get internationalized message
     */
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
