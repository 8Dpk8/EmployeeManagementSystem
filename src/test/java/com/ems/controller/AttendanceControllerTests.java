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
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import com.ems.entity.Attendance;
import com.ems.entity.AttendanceStatus;
import com.ems.entity.Employee;
import com.ems.entity.EmployeeStatus;
import com.ems.entity.Role;
import com.ems.repository.AttendanceRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.LeaveRepository;
import com.ems.repository.PayrollRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AttendanceControllerTests {

	private static final String DATE = "2026-11-24";

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
	private Employee john;

	@BeforeEach
	void setUp() {
		payrollRepository.deleteAll();
		leaveRepository.deleteAll();
		attendanceRepository.deleteAll();
		employeeRepository.deleteAll();
		session = TestLogins.loginAs(Role.ADMIN);
		john = employeeRepository.save(employee("EMP001", "john@ems.com", "IT"));
	}

	@Test
	void checkInThenCheckOut_calculatesAndStoresWorkingHours() throws Exception {
		mockMvc.perform(post("/attendance/check-in").with(session)
						.param("employeeId", john.getId().toString())
						.param("date", DATE)
						.param("time", "09:00"))
				.andExpect(redirectedUrl("/attendance/mark"))
				.andExpect(flash().attribute("message", "John Doe checked in at 09:00 AM"));

		Attendance afterCheckIn = attendanceRepository.findByEmployee_IdAndDate(john.getId(), LocalDate.parse(DATE)).orElseThrow();
		assertThat(afterCheckIn.getCheckIn()).isEqualTo(LocalTime.of(9, 0));
		assertThat(afterCheckIn.getCheckOut()).isNull();
		assertThat(afterCheckIn.getWorkingHours()).isNull();
		assertThat(afterCheckIn.getStatus()).isEqualTo(AttendanceStatus.PRESENT);

		mockMvc.perform(post("/attendance/check-out").with(session)
						.param("employeeId", john.getId().toString())
						.param("date", DATE)
						.param("time", "18:00"))
				.andExpect(redirectedUrl("/attendance/mark"))
				.andExpect(flash().attribute("message", "John Doe checked out at 06:00 PM - worked 9.00 hrs"));

		Attendance saved = attendanceRepository.findByEmployee_IdAndDate(john.getId(), LocalDate.parse(DATE)).orElseThrow();
		assertThat(saved.getCheckOut()).isEqualTo(LocalTime.of(18, 0));
		assertThat(saved.getWorkingHours()).isEqualTo(9.0);
		assertThat(saved.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
		assertThat(attendanceRepository.count()).isEqualTo(1);
	}

	@Test
	void shortDay_isMarkedHalfDay() throws Exception {
		checkIn("09:00");
		mockMvc.perform(post("/attendance/check-out").with(session)
				.param("employeeId", john.getId().toString())
				.param("date", DATE)
				.param("time", "11:30"));

		Attendance saved = attendanceRepository.findByEmployee_IdAndDate(john.getId(), LocalDate.parse(DATE)).orElseThrow();
		assertThat(saved.getWorkingHours()).isEqualTo(2.5);
		assertThat(saved.getStatus()).isEqualTo(AttendanceStatus.HALF_DAY);
	}

	@Test
	void secondCheckInSameDay_isRejected() throws Exception {
		checkIn("09:00");

		mockMvc.perform(post("/attendance/check-in").with(session)
						.param("employeeId", john.getId().toString())
						.param("date", DATE)
						.param("time", "10:00"))
				.andExpect(redirectedUrl("/attendance/mark"))
				.andExpect(flash().attribute("error", "John Doe has already checked in on " + DATE));

		assertThat(attendanceRepository.count()).isEqualTo(1);
	}

	@Test
	void checkOutWithoutCheckIn_isRejected() throws Exception {
		mockMvc.perform(post("/attendance/check-out").with(session)
						.param("employeeId", john.getId().toString())
						.param("date", DATE)
						.param("time", "18:00"))
				.andExpect(flash().attribute("error", "No check-in found for this employee on " + DATE));

		assertThat(attendanceRepository.count()).isZero();
	}

	@Test
	void checkOutBeforeCheckIn_isRejected() throws Exception {
		checkIn("09:00");

		mockMvc.perform(post("/attendance/check-out").with(session)
						.param("employeeId", john.getId().toString())
						.param("date", DATE)
						.param("time", "08:00"))
				.andExpect(flash().attribute("error", "Check-out time must be after check-in time (09:00 AM)"));

		Attendance saved = attendanceRepository.findByEmployee_IdAndDate(john.getId(), LocalDate.parse(DATE)).orElseThrow();
		assertThat(saved.getCheckOut()).isNull();
	}

	@Test
	void list_filtersByDate() throws Exception {
		checkIn("09:00");

		mockMvc.perform(get("/attendance/list").with(session).param("date", DATE))
				.andExpect(status().isOk())
				.andExpect(view().name("attendance/attendance-list"))
				.andExpect(model().attribute("attendances", hasSize(1)));

		mockMvc.perform(get("/attendance/list").with(session).param("date", "2020-01-01"))
				.andExpect(model().attribute("attendances", hasSize(0)));
	}

	@Test
	void markPage_showsActiveEmployees() throws Exception {
		Employee inactive = employee("EMP002", "old@ems.com", "HR");
		inactive.setStatus(EmployeeStatus.INACTIVE);
		employeeRepository.save(inactive);

		mockMvc.perform(get("/attendance/mark").with(session))
				.andExpect(status().isOk())
				.andExpect(view().name("attendance/mark-attendance"))
				.andExpect(model().attribute("employees", hasSize(1)));
	}

	@Test
	void deletingEmployee_alsoDeletesTheirAttendance() throws Exception {
		checkIn("09:00");

		mockMvc.perform(post("/employee/delete/" + john.getId()).with(session))
				.andExpect(redirectedUrl("/employee/list"));

		assertThat(employeeRepository.existsById(john.getId())).isFalse();
		assertThat(attendanceRepository.count()).isZero();
	}

	@Test
	void dashboard_showsCardCounts() throws Exception {
		employeeRepository.save(employee("EMP002", "jane@ems.com", "HR"));
		employeeRepository.save(employee("EMP003", "bob@ems.com", "it"));
		mockMvc.perform(post("/attendance/check-in").with(session)
				.param("employeeId", john.getId().toString())
				.param("date", LocalDate.now().toString())
				.param("time", "09:00"));

		mockMvc.perform(get("/dashboard").with(session))
				.andExpect(status().isOk())
				.andExpect(view().name("dashboard"))
				.andExpect(model().attribute("totalEmployees", 3L))
				.andExpect(model().attribute("presentToday", 1L))
				.andExpect(model().attribute("pendingLeaves", 0L))
				.andExpect(model().attribute("departments", 2L));
	}

	@Test
	void attendancePages_requireLogin() throws Exception {
		mockMvc.perform(get("/attendance/mark"))
				.andExpect(redirectedUrl("/login"));
	}

	private void checkIn(String time) throws Exception {
		mockMvc.perform(post("/attendance/check-in").with(session)
				.param("employeeId", john.getId().toString())
				.param("date", DATE)
				.param("time", time));
	}

	private static Employee employee(String code, String email, String department) {
		return Employee.builder()
				.employeeCode(code)
				.name("John Doe")
				.email(email)
				.department(department)
				.designation("Developer")
				.salary(new BigDecimal("50000"))
				.joiningDate(LocalDate.of(2026, 1, 1))
				.status(EmployeeStatus.ACTIVE)
				.build();
	}
}
