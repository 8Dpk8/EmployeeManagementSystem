package com.ems.controller;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ems.entity.Attendance;
import com.ems.service.AttendanceService;
import com.ems.service.AttendanceService.AttendanceException;
import com.ems.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

	private final AttendanceService attendanceService;
	private final EmployeeService employeeService;

	@GetMapping("/list")
	public String list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			Model model) {
		model.addAttribute("attendances", attendanceService.findAll(date));
		model.addAttribute("selectedDate", date);
		return "attendance/attendance-list";
	}

	@GetMapping("/mark")
	public String markForm(Model model) {
		LocalDate today = LocalDate.now();
		model.addAttribute("employees", employeeService.findActive());
		model.addAttribute("today", today);
		model.addAttribute("now", LocalTime.now().withSecond(0).withNano(0));
		model.addAttribute("todayAttendances", attendanceService.findAll(today));
		return "attendance/mark-attendance";
	}

	@PostMapping("/check-in")
	public String checkIn(@RequestParam(required = false) Long employeeId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
			RedirectAttributes redirectAttributes) {
		try {
			Attendance a = attendanceService.checkIn(employeeId, date, time);
			redirectAttributes.addFlashAttribute("message",
					a.getEmployee().getName() + " checked in at " + a.getCheckInFormatted());
		} catch (AttendanceException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/attendance/mark";
	}

	@PostMapping("/check-out")
	public String checkOut(@RequestParam(required = false) Long employeeId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
			RedirectAttributes redirectAttributes) {
		try {
			Attendance a = attendanceService.checkOut(employeeId, date, time);
			redirectAttributes.addFlashAttribute("message",
					a.getEmployee().getName() + " checked out at " + a.getCheckOutFormatted()
							+ " - worked " + String.format("%.2f", a.getWorkingHours()) + " hrs");
		} catch (AttendanceException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/attendance/mark";
	}
}
