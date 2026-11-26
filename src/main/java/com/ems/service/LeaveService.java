package com.ems.service;

import java.util.EnumSet;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ems.dto.LeaveDTO;
import com.ems.entity.Employee;
import com.ems.entity.LeaveRequest;
import com.ems.entity.LeaveStatus;
import com.ems.entity.Role;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.LeaveRepository;
import com.ems.repository.UserRepository;
import com.ems.util.EmailUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveService {

	private final LeaveRepository leaveRepository;
	private final EmployeeRepository employeeRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;

	@Transactional(readOnly = true)
	public List<LeaveRequest> findAll(LeaveStatus status) {
		return status == null
				? leaveRepository.findAllWithEmployee()
				: leaveRepository.findByStatusWithEmployee(status);
	}

	@Transactional(readOnly = true)
	public long countPending() {
		return leaveRepository.countByStatus(LeaveStatus.PENDING);
	}

	/** Employee applies: creates a new request in PENDING status. */
	public LeaveRequest apply(LeaveDTO dto) {
		Employee employee = employeeRepository.findById(dto.getEmployeeId())
				.orElseThrow(() -> new LeaveException("Employee not found"));
		if (dto.getEndDate().isBefore(dto.getStartDate())) {
			throw new LeaveException("End date cannot be before start date");
		}
		if (leaveRepository.existsOverlapping(employee.getId(), dto.getStartDate(), dto.getEndDate(),
				EnumSet.of(LeaveStatus.PENDING, LeaveStatus.APPROVED))) {
			throw new LeaveException(employee.getName() + " already has a pending or approved leave in these dates");
		}

		LeaveRequest leave = LeaveRequest.builder()
				.employee(employee)
				.leaveType(dto.getLeaveType())
				.startDate(dto.getStartDate())
				.endDate(dto.getEndDate())
				.reason(dto.getReason().trim())
				.status(LeaveStatus.PENDING)
				.build();
		LeaveRequest saved = leaveRepository.save(leave);

		emailService.send(employee.getEmail(), EmailUtil.leaveApplied(saved));
		EmailUtil.EmailContent approverMail = EmailUtil.leaveAwaitingApproval(saved);
		userRepository.findByRoleIn(EnumSet.of(Role.ADMIN, Role.MANAGER))
				.forEach(approver -> emailService.send(approver.getEmail(), approverMail));
		return saved;
	}

	/** Manager approves: PENDING -> APPROVED. */
	public LeaveRequest approve(Long id) {
		return decide(id, LeaveStatus.APPROVED);
	}

	/** Manager rejects: PENDING -> REJECTED. */
	public LeaveRequest reject(Long id) {
		return decide(id, LeaveStatus.REJECTED);
	}

	private LeaveRequest decide(Long id, LeaveStatus newStatus) {
		LeaveRequest leave = leaveRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found: " + id));
		if (leave.getStatus() != LeaveStatus.PENDING) {
			throw new LeaveException("Leave request #" + id + " is already " + leave.getStatus());
		}
		leave.setStatus(newStatus);
		// No explicit save needed: the entity is managed, so the UPDATE runs on commit
		emailService.send(leave.getEmployee().getEmail(), EmailUtil.leaveDecided(leave));
		return leave;
	}

	/** Business-rule violation that should be shown to the user as a message. */
	public static class LeaveException extends RuntimeException {
		public LeaveException(String message) {
			super(message);
		}
	}
}
