package com.ems.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.entity.User;
import com.ems.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security calls loadUserByUsername() when the login form is submitted.
 * We only load the user + role from the DB; Spring Security itself checks the
 * password with the PasswordEncoder bean (BCrypt) from AppConfig.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		String login = usernameOrEmail == null ? "" : usernameOrEmail.trim();
		User user = userRepository.findByUsernameOrEmail(login, login)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));

		return org.springframework.security.core.userdetails.User
				.withUsername(user.getUsername())
				.password(user.getPassword())
				.roles(user.getRole().name()) // ADMIN -> authority "ROLE_ADMIN"
				.build();
	}
}
