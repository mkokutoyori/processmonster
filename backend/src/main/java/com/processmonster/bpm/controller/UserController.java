package com.processmonster.bpm.controller;

import com.processmonster.bpm.dto.user.CreateUserDTO;
import com.processmonster.bpm.dto.user.UpdateUserDTO;
import com.processmonster.bpm.dto.user.UserDTO;
import com.processmonster.bpm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User Management REST Controller
 *
 * Provides endpoints for user CRUD operations and management.
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Get all users (paginated)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER_READ', 'ROLE_ADMIN')")
    @Operation(
        summary = "Get all users",
        description = "Returns a paginated list of all active users"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions"
        )
    })
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Search users by keyword
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('USER_READ', 'ROLE_ADMIN')")
    @Operation(
        summary = "Search users",
        description = "Search users by username, email, first name, or last name"
    )
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam
            @Parameter(description = "Search keyword")
            String keyword,
            @PageableDefault(size = 20)
            Pageable pageable) {
        Page<UserDTO> users = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by role
     */
    @GetMapping("/by-role/{roleName}")
    @PreAuthorize("hasAnyAuthority('USER_READ', 'ROLE_ADMIN')")
    @Operation(
        summary = "Get users by role",
        description = "Returns users with a specific role"
    )
    public ResponseEntity<Page<UserDTO>> getUsersByRole(
            @PathVariable
            @Parameter(description = "Role name (e.g., ROLE_ADMIN)")
            String roleName,
            @PageableDefault(size = 20)
            Pageable pageable) {
        Page<UserDTO> users = userService.getUsersByRole(roleName, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER_READ', 'ROLE_ADMIN')")
    @Operation(
        summary = "Get user by ID",
        description = "Returns detailed information about a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable
            @Parameter(description = "User ID")
            Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Create new user
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER_WRITE', 'ROLE_ADMIN')")
    @Operation(
        summary = "Create new user",
        description = "Creates a new user in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = UserDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or username/email already exists"
        )
    })
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody
            @Parameter(description = "User creation data")
            CreateUserDTO createUserDTO) {
        UserDTO createdUser = userService.createUser(createUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Update existing user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER_WRITE', 'ROLE_ADMIN')")
    @Operation(
        summary = "Update user",
        description = "Updates an existing user's information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable
            @Parameter(description = "User ID")
            Long id,
            @Valid @RequestBody
            @Parameter(description = "User update data")
            UpdateUserDTO updateUserDTO) {
        UserDTO updatedUser = userService.updateUser(id, updateUserDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER_DELETE', 'ROLE_ADMIN')")
    @Operation(
        summary = "Delete user",
        description = "Soft deletes a user (marks as deleted, doesn't remove from database)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User deleted successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Cannot delete yourself"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public ResponseEntity<Void> deleteUser(
            @PathVariable
            @Parameter(description = "User ID")
            Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activate user
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('USER_WRITE', 'ROLE_ADMIN')")
    @Operation(
        summary = "Activate user",
        description = "Enables a deactivated user account"
    )
    public ResponseEntity<UserDTO> activateUser(
            @PathVariable
            @Parameter(description = "User ID")
            Long id) {
        UserDTO user = userService.activateUser(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Deactivate user
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('USER_WRITE', 'ROLE_ADMIN')")
    @Operation(
        summary = "Deactivate user",
        description = "Disables a user account (they won't be able to login)"
    )
    public ResponseEntity<UserDTO> deactivateUser(
            @PathVariable
            @Parameter(description = "User ID")
            Long id) {
        UserDTO user = userService.deactivateUser(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Change user password
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAnyAuthority('USER_WRITE', 'ROLE_ADMIN')")
    @Operation(
        summary = "Change user password",
        description = "Changes a user's password"
    )
    public ResponseEntity<Void> changePassword(
            @PathVariable
            @Parameter(description = "User ID")
            Long id,
            @RequestBody
            @Parameter(description = "New password")
            String newPassword) {
        userService.changePassword(id, newPassword);
        return ResponseEntity.ok().build();
    }
}
