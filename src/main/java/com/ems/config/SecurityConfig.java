package com.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.DispatcherType;

/**
 * All login + role rules live here (Spring Security).
 * Every request passes through this filter chain BEFORE any controller runs.
 * Rules are checked top to bottom; the first matching rule wins.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// 1. WHO can open WHICH URL
				.authorizeHttpRequests(auth -> auth
						// Internal forwards to /WEB-INF/views/*.jsp and error pages
						.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
						// Public pages
						.requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/webjars/**", "/h2-console/**").permitAll()
						// ADMIN only: Employee Management, Payroll, Reports, Salary slip download
						.requestMatchers("/employee/**", "/payroll/**", "/report/**", "/download/**").hasRole("ADMIN")
						// ADMIN or MANAGER: approve / reject leave
						.requestMatchers("/leave/approve/**", "/leave/reject/**").hasAnyRole("ADMIN", "MANAGER")
						// Everything else: any logged-in user
						.anyRequest().authenticated())

				// 2. Login form: GET /login shows our login.jsp, POST /login is handled by Spring Security
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/dashboard", true)
						.failureUrl("/login?error"))

				// 3. Logout: POST /logout clears the session, then redirects
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout"))

				// 4. Logged in but wrong role -> show /access-denied (HTTP 403)
				.exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))

				// H2 console uses frames and its own forms (no CSRF token)
				.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
				.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

		return http.build();
	}
}
