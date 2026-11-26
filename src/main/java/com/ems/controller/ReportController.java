package com.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// ADMIN only - enforced by SecurityConfig: .requestMatchers("/report/**").hasRole("ADMIN")
@Controller
public class ReportController {

	@GetMapping("/report")
	public String reports() {
		return "report/reports";
	}
}
