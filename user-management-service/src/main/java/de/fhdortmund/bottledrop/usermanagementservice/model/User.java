package de.fhdortmund.bottledrop.usermanagementservice.model;

import jakarta.persistence.*;

/**
 * Domain Entity representing a User in the Bottle Drop system.
 * Mapped to the "users" table in the database using JPA/Hibernate.
 * Contains core domain logic for permissions and status changes.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected User() {}

    public User(String name, String email, String passwordHash, Role role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /**
     * Updates the user's system role.
     * @param newRole The newly assigned role.
     */
    public void changeRole(Role newRole) {
        if (newRole != null) {
            this.role = newRole;
        }
    }

    /**
     * Updates the user's password hash.
     * @param newPasswordHash The newly encrypted BCrypt hash.
     */
    public void changePassword(String newPasswordHash) {
        if (newPasswordHash != null && !newPasswordHash.trim().isEmpty()) {
            this.passwordHash = newPasswordHash;
        }
    }

    /**
     * Soft-deletes the user by marking them as inactive.
     * Inactive users cannot authenticate.
     */
    public void deactivateAccount() {
        this.active = false;
    }

    /**
     * Reactivates the user account.
     */
    public void activateAccount() {
        this.active = true;
    }

    /**
     * Updates the user's personal details.
     * @param newName The new name.
     * @param newEmail The new email.
     */
    public void updateDetails(String newName, String newEmail) {
        if (newName != null && !newName.trim().isEmpty()) {
            this.name = newName;
        }
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            this.email = newEmail;
        }
    }

    /**
     * Domain logic: Determines if the user has physical access to drop points.
     * @return true if the role is STOCKIST, BARTENDER, or PLANNER.
     */
    public boolean canVisitDropPoints() {
        return this.role == Role.STOCKIST || this.role == Role.BARTENDER || this.role == Role.PLANNER;
    }

    /**
     * Domain logic: Determines if the user has permission to edit locations.
     * @return true if the user is a PLANNER.
     */
    public boolean canEditLocations() {
        return this.role == Role.PLANNER;
    }

    /**
     * Domain logic: Determines if the user has administrative system privileges.
     * @return true if the user is a PLANNER.
     */
    public boolean isAdmin() {
        return this.role == Role.PLANNER;
    }


    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isActive() { return active; }
    public Role getRole() { return role; }
}