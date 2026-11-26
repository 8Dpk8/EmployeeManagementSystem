package com.ems.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.ems.entity.LeaveType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form data submitted from apply-leave.jsp. Kept separate from the entity so the
 * user can never set fields like id or status from the form.
 */
@Getter
@Setter
@NoArgsConstructor
public class LeaveDTO {

	@NotNull(message = "Please select an employee")
	private Long employeeId;

	@NotNull(message = "Please select a leave type")
	private LeaveType leaveType;

	@NotNull(message = "Start date is required")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate startDate;

	@NotNull(message = "End date is required")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate endDate;

	@NotBlank(message = "Reason is required")
	@Size(max = 500, message = "Reason must be at most 500 characters")
	private String reason;
}
