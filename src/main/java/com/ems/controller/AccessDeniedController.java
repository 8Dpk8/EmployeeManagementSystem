package com.ems.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Shown when a logged-in user opens a page their role is not allowed to see.
 * Spring Security forwards here (see SecurityConfig.exceptionHandling().accessDeniedPage()).
 * @RequestMapping (not @GetMapping) because a denied POST (e.g. /employee/delete) is forwarded as a POST.
 */
@Controller
public class AccessDeniedController {

	@RequestMapping("/access-denied")
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String accessDenied() {
		return "access-denied";
	}
}
