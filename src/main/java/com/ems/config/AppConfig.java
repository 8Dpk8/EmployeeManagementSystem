package com.ems.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ems.entity.Role;
import com.ems.entity.User;
import com.ems.repository.UserRepository;

@Configuration
public class AppConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// Creates one demo login per role (only if missing). Disable with app.seed-demo-users=false.
	@Bean
	@ConditionalOnProperty(name = "app.seed-demo-users", havingValue = "true", matchIfMissing = true)
	public CommandLineRunner seedDemoUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			seedUser(userRepository, passwordEncoder, "admin", "admin@ems.com", "admin123", Role.ADMIN);
			seedUser(userRepository, passwordEncoder, "manager", "manager@ems.com", "manager123", Role.MANAGER);
			seedUser(userRepository, passwordEncoder, "employee", "employee@ems.com", "employee123", Role.EMPLOYEE);
		};
	}

	private static void seedUser(UserRepository repo, PasswordEncoder encoder,
			String username, String email, String rawPassword, Role role) {
		if (repo.existsByUsername(username) || repo.existsByEmail(email)) {
			return;
		}
		repo.save(User.builder()
				.username(username)
				.email(email)
				.password(encoder.encode(rawPassword))
				.role(role)
				.build());
	}
}
