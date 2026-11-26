package com.ems.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ems.entity.Employee;
import com.ems.entity.EmployeeStatus;
import com.ems.repository.AttendanceRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.LeaveRepository;
import com.ems.repository.PayrollRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final AttendanceRepository attendanceRepository;
	private final LeaveRepository leaveRepository;
	private final PayrollRepository payrollRepository;

	@Transactional(readOnly = true)
	public List<Employee> findAll() {
		return employeeRepository.findAll(Sort.by("id"));
	}

	@Transactional(readOnly = true)
	public List<Employee> findActive() {
		return employeeRepository.findByStatusOrderByNameAsc(EmployeeStatus.ACTIVE);
	}

	@Transactional(readOnly = true)
	public long count() {
		return employeeRepository.count();
	}

	@Transactional(readOnly = true)
	public long countDepartments() {
		return employeeRepository.countDistinctDepartments();
	}

	@Transactional(readOnly = true)
	public Employee findById(Long id) {
		return employeeRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + id));
	}

	@Transactional(readOnly = true)
	public boolean isEmployeeCodeTaken(String employeeCode, Long excludeId) {
		String code = trim(employeeCode);
		if (code == null || code.isEmpty()) {
			return false;
		}
		return excludeId == null
				? employeeRepository.existsByEmployeeCodeIgnoreCase(code)
				: employeeRepository.existsByEmployeeCodeIgnoreCaseAndIdNot(code, excludeId);
	}

	@Transactional(readOnly = true)
	public boolean isEmailTaken(String email, Long excludeId) {
		String value = trim(email);
		if (value == null || value.isEmpty()) {
			return false;
		}
		return excludeId == null
				? employeeRepository.existsByEmailIgnoreCase(value)
				: employeeRepository.existsByEmailIgnoreCaseAndIdNot(value, excludeId);
	}

	/**
	 * Creates a new employee when id is null, otherwise updates the existing one.
	 */
	public Employee save(Employee employee) {
		// Without this check, merging an unknown id would silently insert a new row
		if (employee.getId() != null && !employeeRepository.existsById(employee.getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + employee.getId());
		}
		employee.setEmployeeCode(trim(employee.getEmployeeCode()));
		employee.setName(trim(employee.getName()));
		employee.setEmail(trim(employee.getEmail()));
		employee.setPhone(trim(employee.getPhone()));
		employee.setDepartment(trim(employee.getDepartment()));
		employee.setDesignation(trim(employee.getDesignation()));
		return employeeRepository.save(employee);
	}

	public void deleteById(Long id) {
		if (!employeeRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + id);
		}
		// Attendance, leave and payroll rows reference the employee (FK), so remove them first
		attendanceRepository.deleteByEmployeeId(id);
		leaveRepository.deleteByEmployeeId(id);
		payrollRepository.deleteByEmployeeId(id);
		employeeRepository.deleteById(id);
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}
}
