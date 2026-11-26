package com.ems.controller;

import static com.ems.controller.TestLogins.loginAs;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.ems.entity.Attendance;
import com.ems.entity.AttendanceStatus;
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
class DashboardControllerTests {

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

	private final LocalDate today = LocalDate.now();

	@BeforeEach
	void setUp() {
		payrollRepository.deleteAll();
		leaveRepository.deleteAll();
		attendanceRepository.deleteAll();
		employeeRepository.deleteAll();
	}

	@Test
	void chartData_withNoData_returnsZeroFilledSeries() throws Exception {
		mockMvc.perform(get("/dashboard/chart-data").with(loginAs(Role.EMPLOYEE)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.attendanceTrend.labels", hasSize(7)))
				.andExpect(jsonPath("$.attendanceTrend.present", contains(0, 0, 0, 0, 0, 0, 0)))
				.andExpect(jsonPath("$.leaveTrend.labels", hasSize(6)))
				.andExpect(jsonPath("$.leaveTrend.approved", contains(0, 0, 0, 0, 0, 0)))
				.andExpect(jsonPath("$.departmentDistribution.labels", hasSize(0)));
	}

	@Test
	void chartData_countsAttendanceLeavesAndDepartments() throws Exception {
		Employee john = employee("EMP001", "John", "IT");
		Employee jane = employee("EMP002", "Jane", "it");   // same department, different case
		Employee bob = employee("EMP003", "Bob", "HR");

		attendance(john, today, AttendanceStatus.PRESENT);
		attendance(jane, today, AttendanceStatus.PRESENT);
		attendance(bob, today, AttendanceStatus.HALF_DAY);
		attendance(john, today.minusDays(1), AttendanceStatus.ABSENT);
		attendance(john, today.minusDays(30), AttendanceStatus.PRESENT); // outside the 7-day window

		leave(john, today.withDayOfMonth(1), LeaveStatus.APPROVED);
		leave(jane, today.withDayOfMonth(1), LeaveStatus.PENDING);
		leave(bob, today.withDayOfMonth(1).minusMonths(1), LeaveStatus.REJECTED);

		mockMvc.perform(get("/dashboard/chart-data").with(loginAs(Role.ADMIN)))
				.andExpect(status().isOk())
				// last item = today, second last = yesterday
				.andExpect(jsonPath("$.attendanceTrend.present", contains(0, 0, 0, 0, 0, 0, 2)))
				.andExpect(jsonPath("$.attendanceTrend.halfDay", contains(0, 0, 0, 0, 0, 0, 1)))
				.andExpect(jsonPath("$.attendanceTrend.absent", contains(0, 0, 0, 0, 0, 1, 0)))
				// last item = this month, second last = last month
				.andExpect(jsonPath("$.leaveTrend.approved", contains(0, 0, 0, 0, 0, 1)))
				.andExpect(jsonPath("$.leaveTrend.pending", contains(0, 0, 0, 0, 0, 1)))
				.andExpect(jsonPath("$.leaveTrend.rejected", contains(0, 0, 0, 0, 1, 0)))
				// biggest department first; "IT" and "it" merged
				.andExpect(jsonPath("$.departmentDistribution.labels", contains("IT", "HR")))
				.andExpect(jsonPath("$.departmentDistribution.counts", contains(2, 1)));
	}

	@Test
	void chartData_requiresLogin() throws Exception {
		mockMvc.perform(get("/dashboard/chart-data"))
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void frontendLibraries_areServedWithoutLogin() throws Exception {
		mockMvc.perform(get("/webjars/bootstrap/css/bootstrap.min.css")).andExpect(status().isOk());
		mockMvc.perform(get("/webjars/bootstrap/js/bootstrap.bundle.min.js")).andExpect(status().isOk());
		mockMvc.perform(get("/webjars/bootstrap-icons/font/bootstrap-icons.min.css")).andExpect(status().isOk());
		mockMvc.perform(get("/webjars/chart.js/dist/chart.umd.min.js")).andExpect(status().isOk());
	}

	private Employee employee(String code, String name, String department) {
		return employeeRepository.save(Employee.builder()
				.employeeCode(code).name(name).email(code.toLowerCase() + "@ems.com")
				.department(department).designation("Staff").salary(new BigDecimal("30000"))
				.joiningDate(LocalDate.of(2026, 1, 1)).status(EmployeeStatus.ACTIVE)
				.build());
	}

	private void attendance(Employee employee, LocalDate date, AttendanceStatus status) {
		attendanceRepository.save(Attendance.builder().employee(employee).date(date).status(status).build());
	}

	private void leave(Employee employee, LocalDate start, LeaveStatus status) {
		leaveRepository.save(LeaveRequest.builder().employee(employee).leaveType(LeaveType.CASUAL)
				.startDate(start).endDate(start).reason("Personal").status(status).build());
	}
}
