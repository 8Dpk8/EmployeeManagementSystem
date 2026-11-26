package com.ems.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import com.ems.entity.Employee;
import com.ems.entity.EmployeeStatus;
import com.ems.entity.Role;
import com.ems.repository.AttendanceRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.LeaveRepository;
import com.ems.repository.PayrollRepository;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private AttendanceRepository attendanceRepository;

	@Autowired
	private LeaveRepository leaveRepository;

	@Autowired
	private PayrollRepository payrollRepository;

	private RequestPostProcessor session;

	@BeforeEach
	void setUp() {
		payrollRepository.deleteAll();
		leaveRepository.deleteAll();
		attendanceRepository.deleteAll();
		employeeRepository.deleteAll();
		session = TestLogins.loginAs(Role.ADMIN);
	}

	@Test
	void addEmployee_appearsInDatabase() throws Exception {
		mockMvc.perform(post("/employee/save").with(session)
						.param("employeeCode", "EMP001")
						.param("name", "John Doe")
						.param("email", "john@ems.com")
						.param("phone", "9876543210")
						.param("department", "IT")
						.param("designation", "Developer")
						.param("salary", "50000.50")
						.param("joiningDate", "2026-01-15")
						.param("status", "ACTIVE"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/employee/list"))
				.andExpect(flash().attribute("message", "Employee added successfully"));

		Employee saved = employeeRepository.findByEmployeeCode("EMP001").orElseThrow();
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getName()).isEqualTo("John Doe");
		assertThat(saved.getEmail()).isEqualTo("john@ems.com");
		assertThat(saved.getPhone()).isEqualTo("9876543210");
		assertThat(saved.getDepartment()).isEqualTo("IT");
		assertThat(saved.getDesignation()).isEqualTo("Developer");
		assertThat(saved.getSalary()).isEqualByComparingTo("50000.50");
		assertThat(saved.getJoiningDate()).isEqualTo(LocalDate.of(2026, 1, 15));
		assertThat(saved.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
	}

	@Test
	void listEmployees_showsSavedEmployees() throws Exception {
		employeeRepository.save(sampleEmployee("EMP001", "john@ems.com"));

		mockMvc.perform(get("/employee/list").with(session))
				.andExpect(status().isOk())
				.andExpect(view().name("employee/employee-list"))
				.andExpect(model().attribute("employees", hasSize(1)));
	}

	@Test
	void updateEmployee_changesAreSaved() throws Exception {
		Employee existing = employeeRepository.save(sampleEmployee("EMP001", "john@ems.com"));

		mockMvc.perform(get("/employee/edit/" + existing.getId()).with(session))
				.andExpect(status().isOk())
				.andExpect(view().name("employee/edit-employee"));

		mockMvc.perform(post("/employee/save").with(session)
						.param("id", existing.getId().toString())
						.param("employeeCode", "EMP001")
						.param("name", "John Updated")
						.param("email", "john@ems.com")
						.param("phone", "1112223333")
						.param("department", "HR")
						.param("designation", "Manager")
						.param("salary", "75000")
						.param("joiningDate", "2025-06-01")
						.param("status", "INACTIVE"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/employee/list"))
				.andExpect(flash().attribute("message", "Employee updated successfully"));

		assertThat(employeeRepository.count()).isEqualTo(1);
		Employee updated = employeeRepository.findById(existing.getId()).orElseThrow();
		assertThat(updated.getName()).isEqualTo("John Updated");
		assertThat(updated.getDepartment()).isEqualTo("HR");
		assertThat(updated.getDesignation()).isEqualTo("Manager");
		assertThat(updated.getSalary()).isEqualByComparingTo("75000");
		assertThat(updated.getStatus()).isEqualTo(EmployeeStatus.INACTIVE);
	}

	@Test
	void deleteEmployee_removesFromDatabase() throws Exception {
		Employee existing = employeeRepository.save(sampleEmployee("EMP001", "john@ems.com"));

		mockMvc.perform(post("/employee/delete/" + existing.getId()).with(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/employee/list"))
				.andExpect(flash().attribute("message", "Employee deleted successfully"));

		assertThat(employeeRepository.existsById(existing.getId())).isFalse();
	}

	@Test
	void addEmployee_duplicateCodeOrEmail_isRejected() throws Exception {
		employeeRepository.save(sampleEmployee("EMP001", "john@ems.com"));

		mockMvc.perform(post("/employee/save").with(session)
						.param("employeeCode", "EMP001")
						.param("name", "Jane")
						.param("email", "JOHN@ems.com")
						.param("department", "IT")
						.param("designation", "Tester")
						.param("salary", "40000")
						.param("joiningDate", "2026-02-01")
						.param("status", "ACTIVE"))
				.andExpect(status().isOk())
				.andExpect(view().name("employee/add-employee"))
				.andExpect(model().attributeHasFieldErrors("employee", "employeeCode", "email"));

		assertThat(employeeRepository.count()).isEqualTo(1);
	}

	@Test
	void addEmployee_missingRequiredFields_isRejected() throws Exception {
		mockMvc.perform(post("/employee/save").with(session)
						.param("employeeCode", "")
						.param("name", "")
						.param("email", "not-an-email"))
				.andExpect(status().isOk())
				.andExpect(view().name("employee/add-employee"))
				.andExpect(model().attributeHasFieldErrors("employee",
						"employeeCode", "name", "email", "department", "designation", "salary", "joiningDate", "status"));

		assertThat(employeeRepository.count()).isZero();
	}

	@Test
	void editOrDelete_unknownId_returnsNotFound() throws Exception {
		mockMvc.perform(get("/employee/edit/9999").with(session))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/employee/delete/9999").with(session))
				.andExpect(status().isNotFound());
	}

	@Test
	void employeePages_requireLogin() throws Exception {
		mockMvc.perform(get("/employee/list"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	private static Employee sampleEmployee(String code, String email) {
		return Employee.builder()
				.employeeCode(code)
				.name("John Doe")
				.email(email)
				.phone("9876543210")
				.department("IT")
				.designation("Developer")
				.salary(new BigDecimal("50000.00"))
				.joiningDate(LocalDate.of(2026, 1, 15))
				.status(EmployeeStatus.ACTIVE)
				.build();
	}
}
