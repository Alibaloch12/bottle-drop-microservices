package de.fhdortmund.bottledrop.usermanagementservice.dto;

import de.fhdortmund.bottledrop.usermanagementservice.model.Role;

public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private boolean active;
    private Role role;
    private boolean canVisitDropPoints;
    private boolean canEditLocations;
    private boolean isAdmin;

    public UserResponseDTO(Long id, String name, String email, boolean active, Role role,
                           boolean canVisitDropPoints, boolean canEditLocations, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.active = active;
        this.role = role;
        this.canVisitDropPoints = canVisitDropPoints;
        this.canEditLocations = canEditLocations;
        this.isAdmin = isAdmin;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
    public Role getRole() { return role; }
    public boolean isCanVisitDropPoints() { return canVisitDropPoints; }
    public boolean isCanEditLocations() { return canEditLocations; }
    public boolean isAdmin() { return isAdmin; }
}
