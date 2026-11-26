package com.ems.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.entity.Attendance;
import com.ems.entity.AttendanceStatus;
import com.ems.entity.Employee;
import com.ems.repository.AttendanceRepository;
import com.ems.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

	/** Less than this many hours worked counts as a half day. */
	static final double HALF_DAY_THRESHOLD_HOURS = 4.0;

	private final AttendanceRepository attendanceRepository;
	private final EmployeeRepository employeeRepository;

	@Transactional(readOnly = true)
	public List<Attendance> findAll(LocalDate date) {
		return date == null
				? attendanceRepository.findAllWithEmployee()
				: attendanceRepository.findByDateWithEmployee(date);
	}

	@Transactional(readOnly = true)
	public long countPresentOn(LocalDate date) {
		return attendanceRepository.countByDate(date);
	}

	public Attendance checkIn(Long employeeId, LocalDate date, LocalTime time) {
		requireInputs(employeeId, date, time);
		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new AttendanceException("Employee not found"));
		if (attendanceRepository.existsByEmployee_IdAndDate(employeeId, date)) {
			throw new AttendanceException(employee.getName() + " has already checked in on " + date);
		}

		Attendance attendance = Attendance.builder()
				.employee(employee)
				.date(date)
				.checkIn(time.withSecond(0).withNano(0))
				.status(AttendanceStatus.PRESENT)
				.build();
		return attendanceRepository.save(attendance);
	}

	public Attendance checkOut(Long employeeId, LocalDate date, LocalTime time) {
		requireInputs(employeeId, date, time);
		Attendance attendance = attendanceRepository.findByEmployee_IdAndDate(employeeId, date)
				.orElseThrow(() -> new AttendanceException("No check-in found for this employee on " + date));
			if (attendance.getCheckOut() != null) {
			throw new AttendanceException("Already checked out on " + date);
		}
		LocalTime checkOut = time.withSecond(0).withNano(0);
		if (!checkOut.isAfter(attendance.getCheckIn())) {
			throw new AttendanceException("Check-out time must be after check-in time ("
					+ attendance.getCheckInFormatted() + ")");
		}

		double hours = calculateWorkingHours(attendance.getCheckIn(), checkOut);
		attendance.setCheckOut(checkOut);
		attendance.setWorkingHours(hours);
		attendance.setStatus(hours < HALF_DAY_THRESHOLD_HOURS ? AttendanceStatus.HALF_DAY : AttendanceStatus.PRESENT);
		// No explicit save needed: the entity is managed, so changes are flushed on commit
		return attendance;
	}

	/** Hours between check-in and check-out, rounded to 2 decimals (09:00 → 18:00 = 9.0). */
	static double calculateWorkingHours(LocalTime checkIn, LocalTime checkOut) {
		long minutes = Duration.between(checkIn, checkOut).toMinutes();
		return Math.round(minutes / 60.0 * 100) / 100.0;
	}

	private static void requireInputs(Long employeeId, LocalDate date, LocalTime time) {
		if (employeeId == null) {
			throw new AttendanceException("Please select an employee");
		}
		if (date == null) {
			throw new AttendanceException("Please select a date");
		}
		if (time == null) {
			throw new AttendanceException("Please enter a time");
		}
	}

	/** Business-rule violation that should be shown to the user as a message. */
	public static class AttendanceException extends RuntimeException {
		public AttendanceException(String message) {
			super(message);
		}
	}
}
