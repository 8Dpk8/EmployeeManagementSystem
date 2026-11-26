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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.ems.entity.Employee;
import com.ems.entity.EmployeeStatus;
import com.ems.entity.LeaveRequest;
import com.ems.entity.LeaveStatus;
import com.ems.entity.LeaveType;
import com.ems.entity.Role;
import com.ems.repository.AttendanceRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.LeaveRepository;
import com.ems.repository.PayrollRepository;

@SpringBootTest
@AutoConfigureMockMvc
class LeaveControllerTests {

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
	private RequestPostProcessor employeeSession;
	private Employee john;

	@BeforeEach
	void setUp() {
		payrollRepository.deleteAll();
		leaveRepository.deleteAll();
		attendanceRepository.deleteAll();
		employeeRepository.deleteAll();
		adminSession = session(Role.ADMIN);
		employeeSession = session(Role.EMPLOYEE);
		john = employeeRepository.save(Employee.builder()
				.employeeCode("EMP001").name("John Doe").email("john@ems.com")
				.department("IT").designation("Developer").salary(new BigDecimal("50000"))
				.joiningDate(LocalDate.of(2026, 1, 1)).status(EmployeeStatus.ACTIVE)
				.build());
	}

	@Test
	void employeeApplies_requestIsSavedAsPending() throws Exception {
		mockMvc.perform(apply(employeeSession, "2026-12-01", "2026-12-03"))
				.andExpect(redirectedUrl("/leave/list"))
				.andExpect(flash().attribute("message", "Leave applied for John Doe (3 day(s)) - waiting for approval"));

		List<LeaveRequest> all = leaveRepository.findAll();
		assertThat(all).hasSize(1);
		LeaveRequest saved = all.get(0);
		assertThat(saved.getEmployeeId()).isEqualTo(john.getId());
		assertThat(saved.getLeaveType()).isEqualTo(LeaveType.SICK);
		assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2026, 12, 1));
		assertThat(saved.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 3));
		assertThat(saved.getReason()).isEqualTo("Fever");
		assertThat(saved.getStatus()).isEqualTo(LeaveStatus.PENDING);
	}

	@Test
	void managerApproves_statusUpdatedToApproved() throws Exception {
		Long id = applyAndGetId("2026-12-01", "2026-12-03");

		mockMvc.perform(post("/leave/approve/" + id).with(adminSession))
				.andExpect(redirectedUrl("/leave/list"))
				.andExpect(flash().attribute("message", "Leave #" + id + " of John Doe approved"));

		assertThat(leaveRepository.findById(id).orElseThrow().getStatus()).isEqualTo(LeaveStatus.APPROVED);
	}

	@Test
	void managerRejects_statusUpdatedToRejected() throws Exception {
		Long id = applyAndGetId("2026-12-01", "2026-12-03");

		mockMvc.perform(post("/leave/reject/" + id).with(session(Role.MANAGER)))
				.andExpect(redirectedUrl("/leave/list"));

		assertThat(leaveRepository.findById(id).orElseThrow().getStatus()).isEqualTo(LeaveStatus.REJECTED);
	}

	@Test
	void nonManagerCannotApprove() throws Exception {
		Long id = applyAndGetId("2026-12-01", "2026-12-03");

		mockMvc.perform(post("/leave/approve/" + id).with(employeeSession))
				.andExpect(status().isForbidden());

		assertThat(leaveRepository.findById(id).orElseThrow().getStatus()).isEqualTo(LeaveStatus.PENDING);
	}

	@Test
	void alreadyDecidedLeave_cannotBeChangedAgain() throws Exception {
		Long id = applyAndGetId("2026-12-01", "2026-12-03");
		mockMvc.perform(post("/leave/approve/" + id).with(adminSession));

		mockMvc.perform(post("/leave/reject/" + id).with(adminSession))
				.andExpect(flash().attribute("error", "Leave request #" + id + " is already APPROVED"));

		assertThat(leaveRepository.findById(id).orElseThrow().getStatus()).isEqualTo(LeaveStatus.APPROVED);
	}

	@Test
	void endDateBeforeStartDate_isRejected() throws Exception {
		mockMvc.perform(apply(employeeSession, "2026-12-05", "2026-12-01"))
				.andExpect(status().isOk())
				.andExpect(view().name("leave/apply-leave"))
				.andExpect(model().attribute("error", "End date cannot be before start date"));

		assertThat(leaveRepository.count()).isZero();
	}

	@Test
	void overlappingLeave_isRejected() throws Exception {
		applyAndGetId("2026-12-01", "2026-12-05");

		mockMvc.perform(apply(employeeSession, "2026-12-04", "2026-12-08"))
				.andExpect(view().name("leave/apply-leave"))
				.andExpect(model().attribute("error", "John Doe already has a pending or approved leave in these dates"));

		assertThat(leaveRepository.count()).isEqualTo(1);
	}

	@Test
	void rejectedLeave_doesNotBlockNewApplication() throws Exception {
		Long id = applyAndGetId("2026-12-01", "2026-12-05");
		mockMvc.perform(post("/leave/reject/" + id).with(adminSession));

		mockMvc.perform(apply(employeeSession, "2026-12-01", "2026-12-05"))
				.andExpect(redirectedUrl("/leave/list"));

		assertThat(leaveRepository.count()).isEqualTo(2);
	}

	@Test
	void missingFields_showValidationErrors() throws Exception {
		mockMvc.perform(post("/leave/apply").with(employeeSession).param("reason", " "))
				.andExpect(status().isOk())
				.andExpect(view().name("leave/apply-leave"))
				.andExpect(model().attributeHasFieldErrors("leave", "employeeId", "leaveType", "startDate", "endDate", "reason"));

		assertThat(leaveRepository.count()).isZero();
	}

	@Test
	void list_filtersByStatus_andDashboardCountsPending() throws Exception {
		Long first = applyAndGetId("2026-12-01", "2026-12-02");
		applyAndGetId("2026-12-10", "2026-12-11");
		mockMvc.perform(post("/leave/approve/" + first).with(adminSession));

		mockMvc.perform(get("/leave/list").with(adminSession))
				.andExpect(model().attribute("leaves", hasSize(2)))
				.andExpect(model().attribute("canApprove", true));
		mockMvc.perform(get("/leave/list").with(adminSession).param("status", "PENDING"))
				.andExpect(model().attribute("leaves", hasSize(1)));
		mockMvc.perform(get("/leave/list").with(employeeSession))
				.andExpect(model().attribute("canApprove", false));

		mockMvc.perform(get("/dashboard").with(adminSession))
				.andExpect(model().attribute("pendingLeaves", 1L));
	}

	@Test
	void deletingEmployee_alsoDeletesTheirLeaves() throws Exception {
		applyAndGetId("2026-12-01", "2026-12-02");

		mockMvc.perform(post("/employee/delete/" + john.getId()).with(adminSession))
				.andExpect(redirectedUrl("/employee/list"));

		assertThat(leaveRepository.count()).isZero();
	}

	@Test
	void leavePages_requireLogin() throws Exception {
		mockMvc.perform(get("/leave/list")).andExpect(redirectedUrl("/login"));
	}

	private MockHttpServletRequestBuilder apply(RequestPostProcessor session, String start, String end) {
		return post("/leave/apply").with(session)
				.param("employeeId", john.getId().toString())
				.param("leaveType", "SICK")
				.param("startDate", start)
				.param("endDate", end)
				.param("reason", "Fever");
	}

	private Long applyAndGetId(String start, String end) throws Exception {
		mockMvc.perform(apply(employeeSession, start, end)).andExpect(redirectedUrl("/leave/list"));
		return leaveRepository.findAll().stream()
				.filter(l -> l.getStartDate().equals(LocalDate.parse(start)))
				.map(LeaveRequest::getId)
				.max(Long::compare)
				.orElseThrow();
	}

	private static RequestPostProcessor session(Role role) {
		return TestLogins.loginAs(role);
	}
}
