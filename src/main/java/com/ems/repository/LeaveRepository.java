package com.ems.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ems.entity.LeaveRequest;
import com.ems.entity.LeaveStatus;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {

	long countByStatus(LeaveStatus status);

	/** Dashboard "Leave Trend": one row per (year, month, status) -> [Integer year, Integer month, LeaveStatus status, Long count]. */
	@Query("""
			select year(l.startDate), month(l.startDate), l.status, count(l) from LeaveRequest l
			where l.startDate between :from and :to
			group by year(l.startDate), month(l.startDate), l.status
			""")
	List<Object[]> countByMonthAndStatus(@Param("from") LocalDate from, @Param("to") LocalDate to);

	// JOIN FETCH loads the employee in the same query (avoids N+1 selects on the list page)
	@Query("select l from LeaveRequest l join fetch l.employee order by l.id desc")
	List<LeaveRequest> findAllWithEmployee();

	@Query("select l from LeaveRequest l join fetch l.employee where l.status = :status order by l.id desc")
	List<LeaveRequest> findByStatusWithEmployee(@Param("status") LeaveStatus status);

	/** True if the employee already has a leave in the given statuses that overlaps [start, end]. */
	@Query("""
			select count(l) > 0 from LeaveRequest l
			where l.employee.id = :employeeId
			  and l.status in :statuses
			  and l.startDate <= :endDate
			  and l.endDate >= :startDate
			""")
	boolean existsOverlapping(@Param("employeeId") Long employeeId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate,
			@Param("statuses") Collection<LeaveStatus> statuses);

	@Modifying
	@Query("delete from LeaveRequest l where l.employee.id = :employeeId")
	void deleteByEmployeeId(@Param("employeeId") Long employeeId);
}
