package de.fhdortmund.bottledrop.usermanagementservice;

import de.fhdortmund.bottledrop.usermanagementservice.model.Role;
import de.fhdortmund.bottledrop.usermanagementservice.model.User;
import de.fhdortmund.bottledrop.usermanagementservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableDiscoveryClient
public class UserManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserManagementServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner preloadSampleData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.count() == 0) {
				String encodedPassword = passwordEncoder.encode("password123");
				userRepository.save(new User("Ali Murad", "ali@bottledrop.de", encodedPassword, Role.PLANNER));
				userRepository.save(new User("Emma Watson", "emma@bottledrop.de", encodedPassword, Role.WAREHOUSE_ASSISTANT));
				userRepository.save(new User("David Miller", "david@bottledrop.de", encodedPassword, Role.BARTENDER));
				userRepository.save(new User("Sophia Green", "sophia@bottledrop.de", encodedPassword, Role.STOCKIST));
				userRepository.save(new User("John Doe", "john@event.org", encodedPassword, Role.VISITOR));
				System.out.println(">>> Sample Users Database Preloaded Successfully! <<<");
			}
		};
	}
}