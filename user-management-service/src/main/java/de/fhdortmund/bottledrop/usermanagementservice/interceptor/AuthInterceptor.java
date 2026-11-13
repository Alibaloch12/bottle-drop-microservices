package de.fhdortmund.bottledrop.usermanagementservice.interceptor;

import de.fhdortmund.bottledrop.usermanagementservice.dto.UserResponseDTO;
import de.fhdortmund.bottledrop.usermanagementservice.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Allow static resources and authentication routes
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") 
            || uri.equals("/authenticate") || uri.equals("/login-simulation") 
            || uri.equals("/forgot-password") || uri.equals("/error") || uri.equals("/actuator/health")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            UserResponseDTO user = (UserResponseDTO) session.getAttribute("LOGGED_IN_USER");
            if (user != null) {
                // Only PLANNER (Admin) is allowed to access the Dashboard/Users directory directly
                if (user.getRole() == Role.PLANNER) {
                    return true;
                } else {
                    // Logged in, but not an admin, so send them back to login page
                    // (They should have been redirected to their respective services upon login)
                    response.sendRedirect("/authenticate");
                    return false;
                }
            }
        }

        // Not logged in
        response.sendRedirect("/authenticate");
        return false;
    }
}
