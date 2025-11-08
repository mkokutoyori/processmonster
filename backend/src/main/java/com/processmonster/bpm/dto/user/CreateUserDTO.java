package com.processmonster.bpm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for creating a new user
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserDTO {

    @NotBlank(message = "{validation.required}")
    @Size(min = 3, max = 50, message = "{validation.size.min}")
    private String username;

    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.email.invalid}")
    @Size(max = 100, message = "{validation.size.max}")
    private String email;

    @NotBlank(message = "{validation.required}")
    @Size(min = 8, message = "{validation.password.weak}")
    private String password;

    @Size(max = 50, message = "{validation.size.max}")
    private String firstName;

    @Size(max = 50, message = "{validation.size.max}")
    private String lastName;

    @Size(max = 20, message = "{validation.size.max}")
    private String phone;

    private Boolean enabled = true;

    private Set<Long> roleIds;
}
