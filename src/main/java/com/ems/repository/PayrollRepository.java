package com.ems.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ems.entity.Payroll;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

	// JOIN FETCH loads the employee in the same query (avoids N+1 selects on the list page)
	@Query("select p from Payroll p join fetch p.employee order by p.year desc, p.month desc, p.id desc")
	List<Payroll> findAllWithEmployee();

	@Query("""
			select p from Payroll p join fetch p.employee
			where p.month = :month and p.year = :year
			order by p.id desc
			""")
	List<Payroll> findByMonthAndYearWithEmployee(@Param("month") Integer month, @Param("year") Integer year);

	/** One payroll per employee per month: used to block duplicates. */
	@Query("""
			select count(p) > 0 from Payroll p
			where p.employee.id = :employeeId and p.month = :month and p.year = :year
			""")
	boolean existsForEmployeeAndMonth(@Param("employeeId") Long employeeId,
			@Param("month") Integer month,
			@Param("year") Integer year);

	/** All payrolls of one employee, newest first (used to pick the latest salary slip). */
	@Query("""
			select p from Payroll p join fetch p.employee
			where p.employee.id = :employeeId
			order by p.year desc, p.month desc
			""")
	List<Payroll> findByEmployeeIdNewestFirst(@Param("employeeId") Long employeeId);

	/** The payroll of one employee for one specific month. */
	@Query("""
			select p from Payroll p join fetch p.employee
			where p.employee.id = :employeeId and p.month = :month and p.year = :year
			""")
	Optional<Payroll> findForEmployeeAndMonth(@Param("employeeId") Long employeeId,
			@Param("month") Integer month,
			@Param("year") Integer year);

	@Modifying
	@Query("delete from Payroll p where p.employee.id = :employeeId")
	void deleteByEmployeeId(@Param("employeeId") Long employeeId);
}
