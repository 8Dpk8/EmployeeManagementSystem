package com.ems.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.ems.entity.Employee;
import com.ems.entity.EmployeeStatus;
import com.ems.entity.Payroll;
import com.ems.entity.Role;
import com.ems.repository.AttendanceRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.LeaveRepository;
import com.ems.repository.PayrollRepository;
import com.ems.service.PayrollService;

@SpringBootTest
@AutoConfigureMockMvc
class PayrollControllerTests {

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

	private RequestPostProcessor adminSession;
	private Employee john;

	@BeforeEach
	void setUp() {
		payrollRepository.deleteAll();
		leaveRepository.deleteAll();
		attendanceRepository.deleteAll();
		employeeRepository.deleteAll();
		adminSession = TestLogins.loginAs(Role.ADMIN);
		john = employeeRepository.save(Employee.builder()
				.employeeCode("EMP001").name("John Doe").email("john@ems.com")
				.department("IT").designation("Developer").salary(new BigDecimal("50000"))
				.joiningDate(LocalDate.of(2026, 1, 1)).status(EmployeeStatus.ACTIVE)
				.build());
	}

	@Test
	void netSalary_isBasicPlusBonusMinusDeduction() {
		assertThat(PayrollService.calculateNetSalary(new BigDecimal("50000"), new BigDecimal("5000"), new BigDecimal("2000")))
				.isEqualByComparingTo("53000");
		assertThat(PayrollService.calculateNetSalary(new BigDecimal("50000"), null, null))
				.isEqualByComparingTo("50000");
	}

	@Test
	void generate_savesPayrollWithCalculatedNetSalary() throws Exception {
		mockMvc.perform(generate(11, 2026, "50000", "5000", "2000"))
				.andExpect(redirectedUrl("/payroll/list"))
				.andExpect(flash().attribute("message", "Payroll generated for John Doe (11/2026) - Net Salary: 53000.00"));

		Payroll saved = payrollRepository.findAll().get(0);
		assertThat(saved.getEmployeeId()).isEqualTo(john.getId());
		assertThat(saved.getMonth()).isEqualTo(11);
		assertThat(saved.getYear()).isEqualTo(2026);
		assertThat(saved.getBasicSalary()).isEqualByComparingTo("50000");
		assertThat(saved.getBonus()).isEqualByComparingTo("5000");
		assertThat(saved.getDeduction()).isEqualByComparingTo("2000");
		assertThat(saved.getNetSalary()).isEqualByComparingTo("53000");
	}

	@Test
	void netSalarySentFromForm_isIgnored() throws Exception {
		mockMvc.perform(generate(11, 2026, "50000", "0", "0").param("netSalary", "999999"))
				.andExpect(redirectedUrl("/payroll/list"));

		assertThat(payrollRepository.findAll().get(0).getNetSalary()).isEqualByComparingTo("50000");
	}

	@Test
	void duplicatePayrollForSameMonth_isRejected() throws Exception {
		mockMvc.perform(generate(11, 2026, "50000", "0", "0")).andExpect(redirectedUrl("/payroll/list"));

		mockMvc.perform(generate(11, 2026, "50000", "0", "0"))
				.andExpect(view().name("payroll/payroll"))
				.andExpect(model().attribute("error", "Payroll for John Doe for 11/2026 already exists"));

		assertThat(payrollRepository.count()).isEqualTo(1);
	}

	@Test
	void negativeNetSalary_isRejected() throws Exception {
		mockMvc.perform(generate(11, 2026, "1000", "0", "5000"))
				.andExpect(view().name("payroll/payroll"))
				.andExpect(model().attribute("error", "Deduction cannot be more than Basic + Bonus (net salary would be negative)"));

		assertThat(payrollRepository.count()).isZero();
	}

	@Test
	void invalidInput_showsValidationErrors() throws Exception {
		mockMvc.perform(post("/payroll/generate").with(adminSession)
						.param("month", "13").param("bonus", "-1"))
				.andExpect(status().isOk())
				.andExpect(view().name("payroll/payroll"))
				.andExpect(model().attributeHasFieldErrors("payroll", "employeeId", "month", "year", "basicSalary", "bonus"));

		assertThat(payrollRepository.count()).isZero();
	}

	@Test
	void list_showsAll_andFiltersByMonthAndYear() throws Exception {
		mockMvc.perform(generate(10, 2026, "50000", "0", "0"));
		mockMvc.perform(generate(11, 2026, "50000", "0", "0"));

		mockMvc.perform(get("/payroll/list").with(adminSession))
				.andExpect(status().isOk())
				.andExpect(view().name("payroll/payroll"))
				.andExpect(model().attribute("payrolls", hasSize(2)));
		mockMvc.perform(get("/payroll/list").with(adminSession).param("month", "11").param("year", "2026"))
				.andExpect(model().attribute("payrolls", hasSize(1)));
	}

	@Test
	void delete_removesPayroll() throws Exception {
		mockMvc.perform(generate(11, 2026, "50000", "0", "0"));
		Long id = payrollRepository.findAll().get(0).getId();

		mockMvc.perform(post("/payroll/delete/" + id).with(adminSession))
				.andExpect(redirectedUrl("/payroll/list"));

		assertThat(payrollRepository.count()).isZero();
	}

	@Test
	void deletingEmployee_alsoDeletesTheirPayrolls() throws Exception {
		mockMvc.perform(generate(11, 2026, "50000", "0", "0"));

		mockMvc.perform(post("/employee/delete/" + john.getId()).with(adminSession))
				.andExpect(redirectedUrl("/employee/list"));

		assertThat(payrollRepository.count()).isZero();
	}

	@Test
	void nonAdminCannotGeneratePayroll() throws Exception {
		mockMvc.perform(post("/payroll/generate").with(TestLogins.loginAs(Role.MANAGER))
						.param("employeeId", john.getId().toString()).param("month", "11")
						.param("year", "2026").param("basicSalary", "50000"))
				.andExpect(status().isForbidden());

		assertThat(payrollRepository.count()).isZero();
	}

	@Test
	void downloadSalarySlip_returnsPdfAttachment() throws Exception {
		mockMvc.perform(generate(10, 2026, "40000", "0", "0"));
		mockMvc.perform(generate(11, 2026, "50000", "5000", "2000"));

		byte[] latest = mockMvc.perform(get("/download/salarySlip/" + john.getId()).with(adminSession))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF))
				.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"salary-slip.pdf\""))
				.andReturn().getResponse().getContentAsByteArray();
		assertThat(new String(latest, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");

		mockMvc.perform(get("/download/salarySlip/" + john.getId()).with(adminSession)
						.param("month", "10").param("year", "2026"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void downloadSalarySlip_withoutPayroll_returns404() throws Exception {
		mockMvc.perform(get("/download/salarySlip/" + john.getId()).with(adminSession))
				.andExpect(status().isNotFound());
	}

	@Test
	void nonAdminCannotDownloadSalarySlip() throws Exception {
		mockMvc.perform(generate(11, 2026, "50000", "0", "0"));

		mockMvc.perform(get("/download/salarySlip/" + john.getId()).with(TestLogins.loginAs(Role.MANAGER)))
				.andExpect(status().isForbidden());
	}

	private MockHttpServletRequestBuilder generate(int month, int year, String basic, String bonus, String deduction) {
		return post("/payroll/generate").with(adminSession)
				.param("employeeId", john.getId().toString())
				.param("month", String.valueOf(month))
				.param("year", String.valueOf(year))
				.param("basicSalary", basic)
				.param("bonus", bonus)
				.param("deduction", deduction);
	}
}
