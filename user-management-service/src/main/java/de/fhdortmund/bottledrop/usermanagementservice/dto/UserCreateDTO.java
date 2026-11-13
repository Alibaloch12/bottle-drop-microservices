package de.fhdortmund.bottledrop.usermanagementservice.dto;

import de.fhdortmund.bottledrop.usermanagementservice.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserCreateDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    @NotNull(message = "Role must be selected")
    private Role role;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
