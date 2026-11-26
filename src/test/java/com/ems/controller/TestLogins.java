package com.ems.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.ems.entity.Role;

/** MockMvc helper: run a request as a logged-in user with the given role (plus a valid CSRF token for POSTs). */
final class TestLogins {

	private TestLogins() {
	}

	static RequestPostProcessor loginAs(Role role) {
		RequestPostProcessor user = user(role.name().toLowerCase()).roles(role.name());
		RequestPostProcessor csrf = csrf();
		return request -> csrf.postProcessRequest(user.postProcessRequest(request));
	}
}
