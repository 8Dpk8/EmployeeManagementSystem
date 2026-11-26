package com.ems.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ems.dto.PayrollDTO;
import com.ems.entity.Employee;
import com.ems.entity.Payroll;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.PayrollRepository;
import com.ems.util.EmailUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollService {

	private final PayrollRepository payrollRepository;
	private final EmployeeRepository employeeRepository;
	private final EmailService emailService;

	/** Lists all payrolls, or only one month when both month and year are given. */
	@Transactional(readOnly = true)
	public List<Payroll> findAll(Integer month, Integer year) {
		return (month == null || year == null)
				? payrollRepository.findAllWithEmployee()
				: payrollRepository.findByMonthAndYearWithEmployee(month, year);
	}

	/** Net Salary = Basic + Bonus - Deduction */
	public static BigDecimal calculateNetSalary(BigDecimal basic, BigDecimal bonus, BigDecimal deduction) {
		return orZero(basic).add(orZero(bonus)).subtract(orZero(deduction)).setScale(2, RoundingMode.HALF_UP);
	}

	/** Creates the payroll for one employee for one month. */
	public Payroll generate(PayrollDTO dto) {
		Employee employee = employeeRepository.findById(dto.getEmployeeId())
				.orElseThrow(() -> new PayrollException("Employee not found"));
		if (payrollRepository.existsForEmployeeAndMonth(employee.getId(), dto.getMonth(), dto.getYear())) {
			throw new PayrollException("Payroll for " + employee.getName() + " for "
					+ dto.getMonth() + "/" + dto.getYear() + " already exists");
		}

		BigDecimal bonus = orZero(dto.getBonus());
		BigDecimal deduction = orZero(dto.getDeduction());
		BigDecimal netSalary = calculateNetSalary(dto.getBasicSalary(), bonus, deduction);
		if (netSalary.signum() < 0) {
			throw new PayrollException("Deduction cannot be more than Basic + Bonus (net salary would be negative)");
		}

		Payroll payroll = Payroll.builder()
				.employee(employee)
				.month(dto.getMonth())
				.year(dto.getYear())
				.basicSalary(dto.getBasicSalary())
				.bonus(bonus)
				.deduction(deduction)
				.netSalary(netSalary)
				.build();
		Payroll saved = payrollRepository.save(payroll);
		emailService.send(employee.getEmail(), EmailUtil.payrollGenerated(saved));
		return saved;
	}

	public void deleteById(Long id) {
		if (!payrollRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payroll not found: " + id);
		}
		payrollRepository.deleteById(id);
	}

	private static BigDecimal orZero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	/** Business-rule violation that should be shown to the user as a message. */
	public static class PayrollException extends RuntimeException {
		public PayrollException(String message) {
			super(message);
		}
	}
}
