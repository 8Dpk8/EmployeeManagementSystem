package com.ems.controller;

import java.time.LocalDate;
import java.time.Month;

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

import com.ems.dto.PayrollDTO;
import com.ems.entity.Payroll;
import com.ems.service.EmployeeService;
import com.ems.service.PayrollService;
import com.ems.service.PayrollService.PayrollException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// ADMIN only - enforced by SecurityConfig: .requestMatchers("/payroll/**").hasRole("ADMIN")
@Controller
@RequestMapping("/payroll")
@RequiredArgsConstructor
public class PayrollController {

	private final PayrollService payrollService;
	private final EmployeeService employeeService;

	/** Shows the "Generate Payroll" form and the payroll table on the same page. */
	@GetMapping("/list")
	public String list(@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer year,
			Model model) {
		LocalDate today = LocalDate.now();
		PayrollDTO form = new PayrollDTO();
		form.setMonth(today.getMonthValue());
		form.setYear(today.getYear());
		model.addAttribute("payroll", form);
		addPageData(model, month, year);
		return "payroll/payroll";
	}

	@PostMapping("/generate")
	public String generate(@Valid @ModelAttribute("payroll") PayrollDTO payroll,
			BindingResult result,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (!result.hasErrors()) {
			try {
				Payroll saved = payrollService.generate(payroll);
				redirectAttributes.addFlashAttribute("message", "Payroll generated for " + saved.getEmployee().getName()
						+ " (" + saved.getMonth() + "/" + saved.getYear() + ") - Net Salary: " + saved.getNetSalary());
				return "redirect:/payroll/list";
			} catch (PayrollException e) {
				model.addAttribute("error", e.getMessage());
			}
		}
		addPageData(model, null, null);
		return "payroll/payroll";
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		payrollService.deleteById(id);
		redirectAttributes.addFlashAttribute("message", "Payroll #" + id + " deleted");
		return "redirect:/payroll/list";
	}

	private void addPageData(Model model, Integer month, Integer year) {
		model.addAttribute("payrolls", payrollService.findAll(month, year));
		model.addAttribute("employees", employeeService.findActive());
		model.addAttribute("months", Month.values());
		model.addAttribute("selectedMonth", month);
		model.addAttribute("selectedYear", year);
	}
}
