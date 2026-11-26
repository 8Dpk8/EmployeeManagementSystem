package com.ems.dto;

import java.util.List;

/**
 * Data for the three dashboard charts, sent to the browser as JSON by GET /dashboard/chart-data.
 * Each "labels" list is the X axis; the number lists line up with it index by index.
 */
public record DashboardChartsDTO(
		AttendanceTrend attendanceTrend,
		LeaveTrend leaveTrend,
		DepartmentDistribution departmentDistribution) {

	/** Last 7 days: how many attendance records per status per day. */
	public record AttendanceTrend(List<String> labels, List<Long> present, List<Long> halfDay, List<Long> absent) {
	}

	/** Last 6 months: how many leave requests (by start date) per status per month. */
	public record LeaveTrend(List<String> labels, List<Long> approved, List<Long> pending, List<Long> rejected) {
	}

	/** Number of employees in each department. */
	public record DepartmentDistribution(List<String> labels, List<Long> counts) {
	}
}
