package de.fhdortmund.bottledrop.usermanagementservice.config;

import de.fhdortmund.bottledrop.usermanagementservice.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Autowired
    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // Intercept all routes
                .excludePathPatterns(
                        "/authenticate",
                        "/login-simulation",
                        "/forgot-password",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error",
                        "/actuator/**", // Ensure actuator routes are accessible
                        "/api/**"       // Exclude REST API from UI session interceptor
                );
    }
}
