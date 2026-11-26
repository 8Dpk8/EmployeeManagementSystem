package com.ems.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ems.entity.Employee;
import com.ems.entity.EmployeeStatus;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	Optional<Employee> findByEmployeeCode(String employeeCode);

	List<Employee> findByStatusOrderByNameAsc(EmployeeStatus status);

	@Query("select count(distinct lower(e.department)) from Employee e")
	long countDistinctDepartments();

	/** Dashboard "Department Distribution": [String department, Long count]. */
	@Query("select e.department, count(e) from Employee e group by e.department")
	List<Object[]> countByDepartment();

	boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

	boolean existsByEmployeeCodeIgnoreCaseAndIdNot(String employeeCode, Long id);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
