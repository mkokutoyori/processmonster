package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Role Entity
 *
 * Represents a role in the RBAC (Role-Based Access Control) system.
 *
 * Predefined roles:
 * - ROLE_ADMIN: Full system access
 * - ROLE_MANAGER: Process and user management
 * - ROLE_USER: Standard user access
 * - ROLE_ANALYST: Read-only access to reports and analytics
 * - ROLE_AUDITOR: Audit log access
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    @ToString.Exclude
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Add permission to role
     */
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    /**
     * Remove permission from role
     */
    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }
}
