package com.ems.controller;

import static com.ems.controller.TestLogins.loginAs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.ems.entity.Role;
import com.ems.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class RoleAccessTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@ParameterizedTest
	@ValueSource(strings = {"/employee/list", "/employee/add", "/payroll/list", "/report"})
	void adminOnlyPages_openForAdmin(String url) throws Exception {
		mockMvc.perform(get(url).with(loginAs(Role.ADMIN)))
				.andExpect(status().isOk());
	}

	@ParameterizedTest
	@ValueSource(strings = {"/employee/list", "/employee/add", "/payroll/list", "/report"})
	void adminOnlyPages_deniedForManagerAndEmployee(String url) throws Exception {
		for (Role role : new Role[] {Role.MANAGER, Role.EMPLOYEE}) {
			mockMvc.perform(get(url).with(loginAs(role)))
					.andExpect(status().isForbidden())
					.andExpect(forwardedUrl("/access-denied"));
		}
	}

	@Test
	void adminOnlyActions_deniedForEmployee() throws Exception {
		mockMvc.perform(post("/employee/save").with(loginAs(Role.EMPLOYEE)).param("employeeCode", "X"))
				.andExpect(status().isForbidden());
		mockMvc.perform(post("/employee/delete/1").with(loginAs(Role.EMPLOYEE)))
				.andExpect(status().isForbidden());
	}

	@ParameterizedTest
	@ValueSource(strings = {"/dashboard", "/attendance/mark", "/attendance/list", "/leave/list", "/leave/apply"})
	void sharedPages_openForEveryRole(String url) throws Exception {
		for (Role role : Role.values()) {
			mockMvc.perform(get(url).with(loginAs(role)))
					.andExpect(status().isOk());
		}
	}

	@Test
	void accessDeniedPage_returns403() throws Exception {
		mockMvc.perform(get("/access-denied").with(loginAs(Role.EMPLOYEE)))
				.andExpect(status().isForbidden())
				.andExpect(view().name("access-denied"));
		// A denied POST is forwarded as POST, so the page must accept POST too
		mockMvc.perform(post("/access-denied").with(loginAs(Role.EMPLOYEE)))
				.andExpect(status().isForbidden())
				.andExpect(view().name("access-denied"));
	}

	@Test
	void notLoggedIn_goesToLoginNotAccessDenied() throws Exception {
		mockMvc.perform(get("/payroll/list"))
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void postWithoutCsrfToken_isRejected() throws Exception {
		mockMvc.perform(post("/employee/delete/1").with(user("admin").roles("ADMIN")))
				.andExpect(status().isForbidden());
	}

	@Test
	void demoUsers_areSeededForEachRole() {
		assertThat(userRepository.findByUsername("admin").orElseThrow().getRole()).isEqualTo(Role.ADMIN);
		assertThat(userRepository.findByUsername("manager").orElseThrow().getRole()).isEqualTo(Role.MANAGER);
		assertThat(userRepository.findByUsername("employee").orElseThrow().getRole()).isEqualTo(Role.EMPLOYEE);
	}

	@Test
	void login_withUsername_givesRoleFromDatabase() throws Exception {
		mockMvc.perform(formLogin("/login").user("manager").password("manager123"))
				.andExpect(redirectedUrl("/dashboard"))
				.andExpect(authenticated().withUsername("manager").withRoles("MANAGER"));
	}

	@Test
	void login_withEmail_works() throws Exception {
		mockMvc.perform(formLogin("/login").user("admin@ems.com").password("admin123"))
				.andExpect(redirectedUrl("/dashboard"))
				.andExpect(authenticated().withUsername("admin").withRoles("ADMIN"));
	}

	@Test
	void login_wrongPassword_fails() throws Exception {
		mockMvc.perform(formLogin("/login").user("admin").password("wrong"))
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());
	}

	@Test
	void logout_redirectsToLogin() throws Exception {
		mockMvc.perform(post("/logout").with(loginAs(Role.ADMIN)))
				.andExpect(redirectedUrl("/login?logout"))
				.andExpect(unauthenticated());
	}
}
