package com.ems.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Employee code is required")
	@Size(max = 20, message = "Employee code must be at most 20 characters")
	@Column(nullable = false, unique = true, length = 20)
	private String employeeCode;

	@NotBlank(message = "Name is required")
	@Size(max = 100, message = "Name must be at most 100 characters")
	@Column(nullable = false, length = 100)
	private String name;

	@NotBlank(message = "Email is required")
	@Email(message = "Enter a valid email address")
	@Size(max = 100, message = "Email must be at most 100 characters")
	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Pattern(regexp = "^$|^[0-9+\\-() ]{7,20}$", message = "Enter a valid phone number")
	@Column(length = 20)
	private String phone;

	@NotBlank(message = "Department is required")
	@Size(max = 50, message = "Department must be at most 50 characters")
	@Column(nullable = false, length = 50)
	private String department;

	@NotBlank(message = "Designation is required")
	@Size(max = 50, message = "Designation must be at most 50 characters")
	@Column(nullable = false, length = 50)
	private String designation;

	@NotNull(message = "Salary is required")
	@DecimalMin(value = "0.0", message = "Salary cannot be negative")
	@Digits(integer = 10, fraction = 2, message = "Salary can have at most 2 decimal places")
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal salary;

	@NotNull(message = "Joining date is required")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@Column(nullable = false)
	private LocalDate joiningDate;

	@NotNull(message = "Status is required")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EmployeeStatus status;
}
