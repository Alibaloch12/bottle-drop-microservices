package de.fhdortmund.bottledrop.usermanagementservice.controller;

import de.fhdortmund.bottledrop.usermanagementservice.dto.ChangePasswordDTO;
import de.fhdortmund.bottledrop.usermanagementservice.dto.UserCreateDTO;
import de.fhdortmund.bottledrop.usermanagementservice.dto.UserLoginDTO;
import de.fhdortmund.bottledrop.usermanagementservice.dto.UserResponseDTO;
import de.fhdortmund.bottledrop.usermanagementservice.service.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for User Management (BC-1).
 * This controller exposes a standard RESTful API for external microservices
 * or a frontend application to interact with the user data.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves a paginated list of all users in the system.
     * @param pageable Pagination configuration.
     * @return Paginated UserResponseDTO containing user details.
     */
    @GetMapping
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    /**
     * Creates a new user in the system.
     * @param newUser Data transfer object containing the necessary fields to create a user.
     * @return The created user details.
     */
    @PostMapping
    public UserResponseDTO createUser(@RequestBody UserCreateDTO newUser) {
        return userService.createUser(newUser);
    }

    /**
     * Retrieves a single user by their unique ID.
     * @param id The ID of the user.
     * @return The user details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Updates an existing user's details, role, and optionally password.
     * @param id The ID of the user.
     * @param request The data transfer object containing updated details.
     * @return The updated user details.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody de.fhdortmund.bottledrop.usermanagementservice.dto.UserUpdateAdminDTO request) {
        return ResponseEntity.ok(userService.adminUpdateUser(id, request));
    }

    /**
     * Deactivates a user account by setting their active status to false.
     * @param id The ID of the user to deactivate.
     * @return The updated user details.
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<UserResponseDTO> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    /**
     * Activates a user account by setting their active status to true.
     * @param id The ID of the user to activate.
     * @return The updated user details.
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<UserResponseDTO> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    /**
     * Changes a user's password.
     * @param id The ID of the user.
     * @param request The object containing the new password.
     * @return The updated user details.
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<UserResponseDTO> changePassword(@PathVariable Long id, @RequestBody ChangePasswordDTO request) {
        return ResponseEntity.ok(userService.changePassword(id, request.getNewPassword()));
    }

    /**
     * Authenticates a user login request by verifying the email and password combination.
     * @param credentials The login credentials (email and password).
     * @return Authentication success payload with user details and permissions.
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserLoginDTO credentials) {
        UserResponseDTO user = userService.login(credentials);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Authentication successful",
                "userId", user.getId(),
                "name", user.getName(),
                "role", user.getRole(),
                "canVisitDropPoints", user.isCanVisitDropPoints(),
                "canEditLocations", user.isCanEditLocations(),
                "isAdmin", user.isAdmin()
        ));
    }


    // --- TASK 2: Resilience4j Circuit Breaker ---

    /**
     * TASK 2: Resilience4j Circuit Breaker implementation.
     * This endpoint simulates fetching user purchases from another microservice (BC-5).
     * If the remote service fails or times out, the 'purchasesFallback' method will be executed.
     * 
     * @param id The ID of the user whose purchases are being queried.
     * @return Mocked purchase data or a fallback response.
     */
    @GetMapping("/{id}/purchases")
    @CircuitBreaker(name = "purchasesService", fallbackMethod = "purchasesFallback")
    public ResponseEntity<Object> getMockedUserPurchases(@PathVariable Long id) {
        // Just checking if user exists
        userService.getUserById(id);

        // Simulating the data we would eventually get from BC-5
        Map<String, Object> mockedResponse = Map.of(
                "userId", id,
                "status", "MOCKED_DATA",
                "purchases", List.of(
                        Map.of("item", "Club Mate", "amount", 2.50),
                        Map.of("item", "Water", "amount", 1.00)
                )
        );

        return ResponseEntity.ok(mockedResponse);
    }

    /**
     * FALLBACK METHOD: This runs automatically if the main method fails, times out,
     * or if the circuit is currently "open" (tripped).
     */
    @SuppressWarnings("unused") // This tells your IDE to ignore the warning!
    public ResponseEntity<Object> purchasesFallback(Long id, Throwable t) {
        Map<String, Object> fallbackResponse = Map.of(
                "userId", id,
                "status", "FALLBACK_TRIGGERED",
                "message", "The purchasing service is currently unavailable. Please try again later.",
                "error", t.getMessage()
        );
        return ResponseEntity.status(503).body(fallbackResponse);
    }
}