package com.ems.controller;

import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ems.dto.LeaveDTO;
import com.ems.entity.LeaveRequest;
import com.ems.entity.LeaveStatus;
import com.ems.entity.LeaveType;
import com.ems.entity.Role;
import com.ems.service.EmployeeService;
import com.ems.service.LeaveService;
import com.ems.service.LeaveService.LeaveException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/leave")
@RequiredArgsConstructor
public class LeaveController {

	/** Authorities allowed to approve or reject leave (Spring Security adds the "ROLE_" prefix). */
	private static final Set<String> APPROVER_AUTHORITIES = Set.of("ROLE_" + Role.ADMIN.name(), "ROLE_" + Role.MANAGER.name());

	private final LeaveService leaveService;
	private final EmployeeService employeeService;

	/** Used by leave-list.jsp to show/hide the Approve/Reject buttons. */
	@ModelAttribute("canApprove")
	public boolean canApprove(Authentication authentication) {
		return authentication != null && authentication.getAuthorities().stream()
				.anyMatch(a -> APPROVER_AUTHORITIES.contains(a.getAuthority()));
	}

	@GetMapping("/list")
	public String list(@RequestParam(required = false) LeaveStatus status, Model model) {
		model.addAttribute("leaves", leaveService.findAll(status));
		model.addAttribute("selectedStatus", status);
		model.addAttribute("statuses", LeaveStatus.values());
		return "leave/leave-list";
	}

	@GetMapping("/apply")
	public String applyForm(Model model) {
		model.addAttribute("leave", new LeaveDTO());
		addFormData(model);
		return "leave/apply-leave";
	}

	@PostMapping("/apply")
	public String apply(@Valid @ModelAttribute("leave") LeaveDTO leave,
			BindingResult result,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (!result.hasErrors()) {
			try {
				LeaveRequest saved = leaveService.apply(leave);
				redirectAttributes.addFlashAttribute("message", "Leave applied for " + saved.getEmployee().getName()
						+ " (" + saved.getDays() + " day(s)) - waiting for approval");
				return "redirect:/leave/list";
			} catch (LeaveException e) {
				model.addAttribute("error", e.getMessage());
			}
		}
		addFormData(model);
		return "leave/apply-leave";
	}

	@PostMapping("/approve/{id}")
	public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			LeaveRequest leave = leaveService.approve(id);
			redirectAttributes.addFlashAttribute("message", "Leave #" + id + " of " + leave.getEmployee().getName() + " approved");
		} catch (LeaveException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/leave/list";
	}

	@PostMapping("/reject/{id}")
	public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			LeaveRequest leave = leaveService.reject(id);
			redirectAttributes.addFlashAttribute("message", "Leave #" + id + " of " + leave.getEmployee().getName() + " rejected");
		} catch (LeaveException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/leave/list";
	}

	private void addFormData(Model model) {
		model.addAttribute("employees", employeeService.findActive());
		model.addAttribute("leaveTypes", LeaveType.values());
	}
}
