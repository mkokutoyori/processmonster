package com.processmonster.bpm.mapper;

import com.processmonster.bpm.dto.user.CreateUserDTO;
import com.processmonster.bpm.dto.user.UpdateUserDTO;
import com.processmonster.bpm.dto.user.UserDTO;
import com.processmonster.bpm.entity.Permission;
import com.processmonster.bpm.entity.Role;
import com.processmonster.bpm.entity.User;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct Mapper for User entity
 *
 * Converts between User entity and DTOs.
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Convert User entity to UserDTO
     */
    @Mapping(target = "roles", expression = "java(mapRolesToNames(user.getRoles()))")
    @Mapping(target = "permissions", expression = "java(mapPermissionsToNames(user.getRoles()))")
    UserDTO toDTO(User user);

    /**
     * Convert CreateUserDTO to User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    User toEntity(CreateUserDTO dto);

    /**
     * Update User entity from UpdateUserDTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(UpdateUserDTO dto, @MappingTarget User user);

    /**
     * Map roles to role names
     */
    default Set<String> mapRolesToNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
    }

    /**
     * Map roles to permission names (flatten all permissions from all roles)
     */
    default Set<String> mapPermissionsToNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getName)
            .collect(Collectors.toSet());
    }
}
