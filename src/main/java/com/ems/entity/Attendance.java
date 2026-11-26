package com.ems.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One attendance record per employee per day.
 */
@Entity
@Table(name = "attendance",
		uniqueConstraints = @UniqueConstraint(name = "uk_attendance_employee_date", columnNames = {"employee_id", "date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Stored as the employee_id column (FK to employees.id)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Column(name = "date", nullable = false)
	private LocalDate date;

	private LocalTime checkIn;

	private LocalTime checkOut;

	private Double workingHours;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AttendanceStatus status;

	public Long getEmployeeId() {
		return employee == null ? null : employee.getId();
	}

	public String getCheckInFormatted() {
		return checkIn == null ? "-" : checkIn.format(TIME_FORMAT);
	}

	public String getCheckOutFormatted() {
		return checkOut == null ? "-" : checkOut.format(TIME_FORMAT);
	}
}
