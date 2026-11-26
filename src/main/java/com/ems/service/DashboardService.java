package com.ems.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dto.DashboardChartsDTO;
import com.ems.dto.DashboardChartsDTO.AttendanceTrend;
import com.ems.dto.DashboardChartsDTO.DepartmentDistribution;
import com.ems.dto.DashboardChartsDTO.LeaveTrend;
import com.ems.entity.AttendanceStatus;
import com.ems.entity.LeaveStatus;
import com.ems.repository.AttendanceRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.LeaveRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

	static final int ATTENDANCE_DAYS = 7;
	static final int LEAVE_MONTHS = 6;

	private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
	private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

	private final AttendanceRepository attendanceRepository;
	private final LeaveRepository leaveRepository;
	private final EmployeeRepository employeeRepository;

	public DashboardChartsDTO chartData(LocalDate today) {
		return new DashboardChartsDTO(attendanceTrend(today), leaveTrend(today), departmentDistribution());
	}

	AttendanceTrend attendanceTrend(LocalDate today) {
		LocalDate from = today.minusDays(ATTENDANCE_DAYS - 1);

		// key = date + status, value = count (days/statuses with no rows stay 0)
		Map<String, Long> counts = new HashMap<>();
		for (Object[] row : attendanceRepository.countByDateAndStatus(from, today)) {
			counts.put(row[0] + "|" + row[1], (Long) row[2]);
		}

		List<String> labels = new ArrayList<>();
		List<Long> present = new ArrayList<>();
		List<Long> halfDay = new ArrayList<>();
		List<Long> absent = new ArrayList<>();
		for (LocalDate day = from; !day.isAfter(today); day = day.plusDays(1)) {
			labels.add(day.format(DAY_LABEL));
			present.add(counts.getOrDefault(day + "|" + AttendanceStatus.PRESENT, 0L));
			halfDay.add(counts.getOrDefault(day + "|" + AttendanceStatus.HALF_DAY, 0L));
			absent.add(counts.getOrDefault(day + "|" + AttendanceStatus.ABSENT, 0L));
		}
		return new AttendanceTrend(labels, present, halfDay, absent);
	}

	LeaveTrend leaveTrend(LocalDate today) {
		YearMonth last = YearMonth.from(today);
		YearMonth first = last.minusMonths(LEAVE_MONTHS - 1);

		Map<String, Long> counts = new HashMap<>();
		for (Object[] row : leaveRepository.countByMonthAndStatus(first.atDay(1), last.atEndOfMonth())) {
			YearMonth month = YearMonth.of(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
			counts.put(month + "|" + row[2], (Long) row[3]);
		}

		List<String> labels = new ArrayList<>();
		List<Long> approved = new ArrayList<>();
		List<Long> pending = new ArrayList<>();
		List<Long> rejected = new ArrayList<>();
		for (YearMonth month = first; !month.isAfter(last); month = month.plusMonths(1)) {
			labels.add(month.format(MONTH_LABEL));
			approved.add(counts.getOrDefault(month + "|" + LeaveStatus.APPROVED, 0L));
			pending.add(counts.getOrDefault(month + "|" + LeaveStatus.PENDING, 0L));
			rejected.add(counts.getOrDefault(month + "|" + LeaveStatus.REJECTED, 0L));
		}
		return new LeaveTrend(labels, approved, pending, rejected);
	}

	DepartmentDistribution departmentDistribution() {
		// "IT" and "it " are the same department: group case-insensitively, keep the first spelling as the label
		Map<String, String> labelByKey = new LinkedHashMap<>();
		Map<String, Long> countByKey = new HashMap<>();
		for (Object[] row : employeeRepository.countByDepartment()) {
			String name = ((String) row[0]).trim();
			String key = name.toLowerCase(Locale.ROOT);
			labelByKey.putIfAbsent(key, name);
			countByKey.merge(key, (Long) row[1], Long::sum);
		}

		List<String> keys = new ArrayList<>(labelByKey.keySet());
		keys.sort(Comparator.comparing(countByKey::get, Comparator.reverseOrder())); // biggest department first

		List<String> labels = new ArrayList<>();
		List<Long> values = new ArrayList<>();
		for (String key : keys) {
			labels.add(labelByKey.get(key));
			values.add(countByKey.get(key));
		}
		return new DepartmentDistribution(labels, values);
	}
}
