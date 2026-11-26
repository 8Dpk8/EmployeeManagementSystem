package com.ems.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ems.entity.Payroll;
import com.ems.repository.PayrollRepository;
import com.ems.util.PdfGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PdfService {

	private final PayrollRepository payrollRepository;

	/**
	 * Salary slip for the given month/year, or the employee's latest payroll
	 * when month/year are not given. 404 if no payroll exists.
	 */
	public byte[] salarySlip(Long employeeId, Integer month, Integer year) {
		Payroll payroll = (month != null && year != null)
				? payrollRepository.findForEmployeeAndMonth(employeeId, month, year).orElse(null)
				: payrollRepository.findByEmployeeIdNewestFirst(employeeId).stream().findFirst().orElse(null);
		if (payroll == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No payroll found for employee " + employeeId);
		}
		return PdfGenerator.salarySlip(payroll);
	}
}
