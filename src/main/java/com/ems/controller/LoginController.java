package com.ems.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Only SHOWS the login page.
 * POST /login (check password) and POST /logout are handled by Spring Security
 * itself - see SecurityConfig.formLogin() / logout().
 */
@Controller
public class LoginController {

	@GetMapping({"/", "/login"})
	public String login(@RequestParam(required = false) String error,
			@RequestParam(required = false) String logout,
			Principal principal,
			Model model) {
		if (principal != null) {
			return "redirect:/dashboard";
		}
		if (error != null) {
			model.addAttribute("error", "Invalid username/email or password");
		}
		if (logout != null) {
			model.addAttribute("message", "You have been logged out");
		}
		return "login";
	}
}
