package de.fhdortmund.bottledrop.usermanagementservice.service;

import de.fhdortmund.bottledrop.usermanagementservice.dto.UserCreateDTO;
import de.fhdortmund.bottledrop.usermanagementservice.dto.UserLoginDTO;
import de.fhdortmund.bottledrop.usermanagementservice.dto.UserResponseDTO;
import de.fhdortmund.bottledrop.usermanagementservice.dto.UserUpdateDTO;
import de.fhdortmund.bottledrop.usermanagementservice.exception.InvalidCredentialsException;
import de.fhdortmund.bottledrop.usermanagementservice.exception.UserNotFoundException;
import de.fhdortmund.bottledrop.usermanagementservice.model.User;
import de.fhdortmund.bottledrop.usermanagementservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core Service layer for User Management (BC-1).
 * This class isolates the business logic from the web and API controllers.
 * It handles database interactions via UserRepository, password encoding,
 * and mapping database Entities to DTOs for safe external use.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves users from the database with pagination and maps them to secure DTOs.
     * @param pageable Pagination configuration.
     * @return A paginated list of mapped UserResponseDTO objects.
     */
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Retrieves a single user by ID.
     * Throws an exception if the user does not exist.
     * @param id The database ID of the user.
     * @return The mapped UserResponseDTO.
     */
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    /**
     * Creates a new user in the database.
     * Hashes the raw password using BCrypt before persisting.
     * @param dto The request payload containing new user details.
     * @return The created user as a DTO.
     */
    public UserResponseDTO createUser(UserCreateDTO dto) {
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User newUser = new User(dto.getName(), dto.getEmail(), encodedPassword, dto.getRole());
        User savedUser = userRepository.save(newUser);
        return mapToDTO(savedUser);
    }

    /**
     * Deactivates a user account (soft delete concept).
     * @param id The ID of the user to deactivate.
     * @return The deactivated user as a DTO.
     */
    public UserResponseDTO deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.deactivateAccount();
        return mapToDTO(userRepository.save(user));
    }

    /**
     * Reactivates a user account.
     * @param id The ID of the user to activate.
     * @return The activated user as a DTO.
     */
    public UserResponseDTO activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.activateAccount();
        return mapToDTO(userRepository.save(user));
    }

    /**
     * Updates the password for a specific user.
     * The new password is encrypted via BCrypt before saving.
     * @param id The ID of the user.
     * @param newPassword The raw new password.
     * @return The updated user as a DTO.
     */
    public UserResponseDTO changePassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.changePassword(passwordEncoder.encode(newPassword));
        return mapToDTO(userRepository.save(user));
    }

    /**
     * Updates the user's profile details.
     * @param id The user ID.
     * @param dto The new name and email.
     * @return The updated user as a DTO.
     */
    public UserResponseDTO updateProfile(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Verify current password before allowing updates
        if (dto.getCurrentPassword() == null || !passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid current password");
        }

        user.updateDetails(dto.getName(), dto.getEmail());
        
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.changePassword(passwordEncoder.encode(dto.getPassword()));
        }
        
        return mapToDTO(userRepository.save(user));
    }

    /**
     * Admin updates a user's details, role, and optionally password without needing the current password.
     * @param id The user ID.
     * @param dto The admin update DTO.
     * @return The updated user as a DTO.
     */
    public UserResponseDTO adminUpdateUser(Long id, de.fhdortmund.bottledrop.usermanagementservice.dto.UserUpdateAdminDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.updateDetails(dto.getName(), dto.getEmail());
        if (dto.getRole() != null) {
            user.changeRole(dto.getRole());
        }
        
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.changePassword(passwordEncoder.encode(dto.getPassword()));
        }
        
        return mapToDTO(userRepository.save(user));
    }

    /**
     * Resets the user's password to a secure temporary default based on their email.
     * @param email The user's email.
     * @return The new temporary password.
     */
    public String resetPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        String tempPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        user.changePassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
        return tempPassword;
    }

    /**
     * Authenticates a user based on their email and raw password.
     * Verifies that the account is active and that the password matches the BCrypt hash.
     * @param dto The login request payload.
     * @return The authenticated user as a DTO if successful.
     * @throws UserNotFoundException if the email is not registered.
     * @throws InvalidCredentialsException if the account is inactive or password does not match.
     */
    public UserResponseDTO login(UserLoginDTO dto) {
        if (dto.getEmail() == null || dto.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is inactive");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return mapToDTO(user);
    }

    /**
     * Helper method to map a User Entity to a UserResponseDTO.
     * This ensures that sensitive fields (like password hashes) are never
     * exposed to the API controllers or views.
     * @param user The database User Entity.
     * @return A secure UserResponseDTO.
     */
    private UserResponseDTO mapToDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isActive(),
                user.getRole(),
                user.canVisitDropPoints(),
                user.canEditLocations(),
                user.isAdmin()
        );
    }
}
