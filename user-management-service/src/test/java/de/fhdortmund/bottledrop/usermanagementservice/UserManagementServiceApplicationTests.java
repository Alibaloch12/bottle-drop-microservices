package de.fhdortmund.bottledrop.usermanagementservice;

import de.fhdortmund.bottledrop.usermanagementservice.dto.UserCreateDTO;
import de.fhdortmund.bottledrop.usermanagementservice.dto.UserLoginDTO;
import de.fhdortmund.bottledrop.usermanagementservice.dto.UserResponseDTO;
import de.fhdortmund.bottledrop.usermanagementservice.exception.InvalidCredentialsException;
import de.fhdortmund.bottledrop.usermanagementservice.model.Role;
import de.fhdortmund.bottledrop.usermanagementservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserManagementServiceApplicationTests {

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
        assertNotNull(userService);
    }

    @Test
    void testUserDomainLogic() {
        // 1. Create user and save via UserService
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setName("Test User");
        createDTO.setEmail("test@dmsa.org");
        createDTO.setPassword("secret123");
        createDTO.setRole(Role.BARTENDER);
        
        UserResponseDTO saved = userService.createUser(createDTO);

        assertNotNull(saved.getId());
        assertEquals("Test User", saved.getName());
        assertEquals("test@dmsa.org", saved.getEmail());
        assertEquals(Role.BARTENDER, saved.getRole());
        assertTrue(saved.isActive());
        assertTrue(saved.isCanVisitDropPoints());
        assertFalse(saved.isCanEditLocations());
        assertFalse(saved.isAdmin());

        // 2. Test authentication
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setEmail("test@dmsa.org");
        loginDTO.setPassword("secret123");
        assertNotNull(userService.login(loginDTO));
        
        loginDTO.setPassword("wrongpassword");
        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginDTO));

        // 3. Test change password
        userService.changePassword(saved.getId(), "newsecret");
        
        loginDTO.setPassword("newsecret");
        assertNotNull(userService.login(loginDTO)); // new password works
        
        loginDTO.setPassword("secret123");
        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginDTO)); // old fails

        // 4. Test deactivation
        userService.deactivateUser(saved.getId());
        
        loginDTO.setPassword("newsecret");
        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginDTO)); // Inactive user cannot login
    }
}
