package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Permission Entity
 *
 * Represents a granular permission in the RBAC system.
 *
 * Permission naming convention: RESOURCE_ACTION
 * Examples:
 * - USER_READ, USER_WRITE, USER_DELETE
 * - PROCESS_READ, PROCESS_WRITE, PROCESS_EXECUTE
 * - TASK_READ, TASK_WRITE, TASK_COMPLETE
 * - REPORT_READ, REPORT_GENERATE
 * - AUDIT_READ
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @Size(max = 50)
    @Column(length = 50)
    private String resource;

    @Size(max = 50)
    @Column(length = 50)
    private String action;
}
