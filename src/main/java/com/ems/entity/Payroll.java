package com.ems.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "payrolls", uniqueConstraints = @UniqueConstraint(
		name = "uk_payroll_employee_month_year", columnNames = {"employee_id", "pay_month", "pay_year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Stored as the employee_id column (FK to employees.id)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	// "month" and "year" are reserved words in H2/SQL, so use safe column names
	@Column(name = "pay_month", nullable = false)
	private Integer month;

	@Column(name = "pay_year", nullable = false)
	private Integer year;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal basicSalary;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal bonus;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal deduction;

	/** Net Salary = Basic + Bonus - Deduction (calculated by PayrollService, never by the form). */
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal netSalary;

	public Long getEmployeeId() {
		return employee == null ? null : employee.getId();
	}
}
