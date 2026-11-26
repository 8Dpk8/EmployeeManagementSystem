package com.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ems.entity.Employee;
import com.ems.entity.EmployeeStatus;
import com.ems.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

	private static final String LIST_VIEW = "employee/employee-list";
	private static final String ADD_VIEW = "employee/add-employee";
	private static final String EDIT_VIEW = "employee/edit-employee";

	private final EmployeeService employeeService;

	@ModelAttribute("statuses")
	public EmployeeStatus[] statuses() {
		return EmployeeStatus.values();
	}

	@GetMapping("/list")
	public String list(Model model) {
		model.addAttribute("employees", employeeService.findAll());
		return LIST_VIEW;
	}

	@GetMapping("/add") // when we click on edit button the data is pre filled due to this
	public String add(Model model) {
		Employee employee = new Employee();
		employee.setStatus(EmployeeStatus.ACTIVE);
		model.addAttribute("employee", employee);
		return ADD_VIEW;
	}

	@PostMapping("/save")
	public String save(@Valid @ModelAttribute("employee") Employee employee,
			BindingResult result,
			RedirectAttributes redirectAttributes) {
		boolean isNew = employee.getId() == null;

		if (!result.hasFieldErrors("employeeCode")
				&& employeeService.isEmployeeCodeTaken(employee.getEmployeeCode(), employee.getId())) {
			result.rejectValue("employeeCode", "duplicate", "Employee code already exists");
		}
		if (!result.hasFieldErrors("email")
				&& employeeService.isEmailTaken(employee.getEmail(), employee.getId())) {
			result.rejectValue("email", "duplicate", "Email already exists");
		}
		if (result.hasErrors()) {
			return isNew ? ADD_VIEW : EDIT_VIEW;
		}

		employeeService.save(employee);
		redirectAttributes.addFlashAttribute("message",
				isNew ? "Employee added successfully" : "Employee updated successfully");
		return "redirect:/employee/list";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model) {
		model.addAttribute("employee", employeeService.findById(id));
		return EDIT_VIEW;
	}

	// POST (not GET) so that a link, crawler or prefetch can never delete data
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		employeeService.deleteById(id);
		redirectAttributes.addFlashAttribute("message", "Employee deleted successfully");
		return "redirect:/employee/list";
	}
}
