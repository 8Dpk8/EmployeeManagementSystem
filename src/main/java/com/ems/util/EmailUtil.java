package com.ems.util;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import com.ems.entity.LeaveRequest;
import com.ems.entity.Payroll;

/** Builds the subject/body text of every notification email. */
public final class EmailUtil {

	private EmailUtil() {
	}

	public record EmailContent(String subject, String body) {
	}

	public static EmailContent leaveApplied(LeaveRequest leave) {
		return new EmailContent(
				"Leave request #" + leave.getId() + " submitted",
				"Hello " + leave.getEmployee().getName() + ",\n\n"
						+ "Your leave request has been submitted and is awaiting approval.\n\n"
						+ leaveDetails(leave) + signature());
	}

	public static EmailContent leaveAwaitingApproval(LeaveRequest leave) {
		return new EmailContent(
				"New leave request from " + leave.getEmployee().getName(),
				"Hello,\n\n"
						+ leave.getEmployee().getName() + " (" + leave.getEmployee().getEmployeeCode()
						+ ") has applied for leave. Please review it in EMS.\n\n"
						+ leaveDetails(leave) + signature());
	}

	public static EmailContent leaveDecided(LeaveRequest leave) {
		String status = leave.getStatus().name();
		return new EmailContent(
				"Leave request #" + leave.getId() + " " + status,
				"Hello " + leave.getEmployee().getName() + ",\n\n"
						+ "Your leave request has been " + status + ".\n\n"
						+ leaveDetails(leave) + signature());
	}

	public static EmailContent payrollGenerated(Payroll payroll) {
		String period = Month.of(payroll.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + payroll.getYear();
		return new EmailContent(
				"Salary processed for " + period,
				"Hello " + payroll.getEmployee().getName() + ",\n\n"
						+ "Your salary for " + period + " has been processed.\n\n"
						+ "Basic Salary : " + payroll.getBasicSalary() + "\n"
						+ "Bonus        : " + payroll.getBonus() + "\n"
						+ "Deduction    : " + payroll.getDeduction() + "\n"
						+ "Net Salary   : " + payroll.getNetSalary() + "\n\n"
						+ "Your salary slip is available from HR." + signature());
	}

	private static String leaveDetails(LeaveRequest leave) {
		return "Leave Type : " + leave.getLeaveType() + "\n"
				+ "From       : " + leave.getStartDate() + "\n"
				+ "To         : " + leave.getEndDate() + "\n"
				+ "Days       : " + leave.getDays() + "\n"
				+ "Reason     : " + leave.getReason() + "\n"
				+ "Status     : " + leave.getStatus() + "\n";
	}

	private static String signature() {
		return "\n\nRegards,\nEmployee Management System";
	}
}
