package com.ems.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Stored as the employee_id column (FK to employees.id)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LeaveType leaveType;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false, length = 500)
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LeaveStatus status;

	public Long getEmployeeId() {
		return employee == null ? null : employee.getId();
	}

	/** Number of calendar days, both start and end date included. */
	public long getDays() {
		if (startDate == null || endDate == null) {
			return 0;
		}
		return ChronoUnit.DAYS.between(startDate, endDate) + 1;
	}
}
