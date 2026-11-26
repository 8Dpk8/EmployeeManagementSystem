package com.ems.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form data submitted from payroll.jsp. netSalary is intentionally NOT here:
 * the server always calculates it, so the user can't type a wrong value.
 */
@Getter
@Setter
@NoArgsConstructor
public class PayrollDTO {

	@NotNull(message = "Please select an employee")
	private Long employeeId;

	@NotNull(message = "Month is required")
	@Min(value = 1, message = "Month must be between 1 and 12")
	@Max(value = 12, message = "Month must be between 1 and 12")
	private Integer month;

	@NotNull(message = "Year is required")
	@Min(value = 2000, message = "Year must be 2000 or later")
	@Max(value = 2100, message = "Year must be 2100 or earlier")
	private Integer year;

	@NotNull(message = "Basic salary is required")
	@DecimalMin(value = "0.0", message = "Basic salary cannot be negative")
	@Digits(integer = 10, fraction = 2, message = "Basic salary can have at most 2 decimal places")
	private BigDecimal basicSalary;

	@DecimalMin(value = "0.0", message = "Bonus cannot be negative")
	@Digits(integer = 10, fraction = 2, message = "Bonus can have at most 2 decimal places")
	private BigDecimal bonus = BigDecimal.ZERO;

	@DecimalMin(value = "0.0", message = "Deduction cannot be negative")
	@Digits(integer = 10, fraction = 2, message = "Deduction can have at most 2 decimal places")
	private BigDecimal deduction = BigDecimal.ZERO;
}
