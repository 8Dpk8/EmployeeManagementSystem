package com.ems.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ems.dto.DashboardChartsDTO;
import com.ems.service.AttendanceService;
import com.ems.service.DashboardService;
import com.ems.service.EmployeeService;
import com.ems.service.LeaveService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashboardController {

	private final EmployeeService employeeService;
	private final AttendanceService attendanceService;
	private final LeaveService leaveService;
	private final DashboardService dashboardService;

	// Login is enforced by SecurityConfig's interceptor
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		LocalDate today = LocalDate.now();
		model.addAttribute("today", today);
		model.addAttribute("totalEmployees", employeeService.count());
		model.addAttribute("presentToday", attendanceService.countPresentOn(today));
		model.addAttribute("pendingLeaves", leaveService.countPending());
		model.addAttribute("departments", employeeService.countDepartments());
		return "dashboard";
	}

	/** JSON for the Chart.js graphs; loaded by js/dashboard.js after the page opens. */
	@GetMapping("/dashboard/chart-data")
	@ResponseBody
	public DashboardChartsDTO chartData() {
		return dashboardService.chartData(LocalDate.now());
	}
}