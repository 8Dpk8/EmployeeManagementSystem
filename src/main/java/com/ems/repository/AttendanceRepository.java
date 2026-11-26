package com.ems.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ems.entity.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

	Optional<Attendance> findByEmployee_IdAndDate(Long employeeId, LocalDate date);

	boolean existsByEmployee_IdAndDate(Long employeeId, LocalDate date);

	long countByDate(LocalDate date);

	/** Dashboard "Attendance Trend": one row per (date, status) -> [LocalDate date, AttendanceStatus status, Long count]. */
	@Query("""
			select a.date, a.status, count(a) from Attendance a
			where a.date between :from and :to
			group by a.date, a.status
			""")
	List<Object[]> countByDateAndStatus(@Param("from") LocalDate from, @Param("to") LocalDate to);

	// JOIN FETCH loads the employee in the same query (avoids N+1 selects on the list page)
	@Query("select a from Attendance a join fetch a.employee order by a.date desc, a.checkIn desc")
	List<Attendance> findAllWithEmployee();

	@Query("select a from Attendance a join fetch a.employee where a.date = :date order by a.checkIn desc")
	List<Attendance> findByDateWithEmployee(@Param("date") LocalDate date);

	@Modifying
	@Query("delete from Attendance a where a.employee.id = :employeeId")
	void deleteByEmployeeId(@Param("employeeId") Long employeeId);
}
