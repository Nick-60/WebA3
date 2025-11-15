package com.example.leave.service;

import com.example.leave.model.LeaveRequest;
import com.example.leave.model.LeaveStatus;
import com.example.leave.model.LeaveType;
import com.example.leave.model.AuditLog;
import com.example.leave.model.User;
import com.example.leave.model.UserRole;
import com.example.leave.repository.LeaveRequestRepository;
import com.example.leave.repository.AuditLogRepository;
import com.example.leave.repository.UserRepository;
import com.example.leave.service.MailService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Autowired
    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        AuditLogRepository auditLogRepository,
                        UserRepository userRepository,
                        MailService mailService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    @Transactional
    public LeaveRequest createLeave(User currentUser, LeaveType leaveType, LocalDate start, LocalDate end, String comment) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start_date and end_date must not be null");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end_date must not be earlier than start_date");
        }

        Long empId = Objects.requireNonNull(currentUser.getId(), "currentUser.id must not be null");
        List<LeaveStatus> activeStatuses = Arrays.asList(LeaveStatus.PENDING, LeaveStatus.APPROVED);
        boolean overlap = leaveRequestRepository
                .existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        empId, activeStatuses, end, start);
        if (overlap) {
            throw new IllegalArgumentException("Requested date range overlaps with existing leave records");
        }

        long daysCount = ChronoUnit.DAYS.between(start, end) + 1;
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId(empId);
        lr.setLeaveType(leaveType);
        lr.setStartDate(start);
        lr.setEndDate(end);
        lr.setDays(BigDecimal.valueOf(daysCount));
        lr.setStatus(LeaveStatus.PENDING);
        // 员工备注存 employeeComment；审批备注在审批阶段写入 approvalComment
        lr.setEmployeeComment(comment);

        LeaveRequest saved = leaveRequestRepository.save(lr);

        AuditLog log = new AuditLog();
        log.setEntity("leave_requests");
        log.setEntityId(saved.getId());
        log.setAction("CREATE");
        log.setUserId(empId);
        log.setDetails(String.format("{\"employeeId\":%d,\"range\":\"%s~%s\",\"days\":%d}",
                currentUser.getId(), start, end, daysCount));
        auditLogRepository.save(log);

        // Email notification: notify manager after submission
        userRepository.findManagerByEmployeeId(empId).ifPresent(manager -> {
            String subject = "Leave request submitted";
            String text = String.format(
                    "Employee %s(%s) submitted a leave request: %s %s ~ %s, %d day(s). ID: %d.",
                    currentUser.getUsername(), currentUser.getEmail(),
                    leaveType.name(), start, end, daysCount, saved.getId());
            mailService.send(manager.getEmail(), subject, text);
        });

        return saved;
    }

    public Page<LeaveRequest> listEmployeeLeaves(String username, int page, int size) {
        Optional<User> ou = userRepository.findByUsername(username);
        if (ou.isEmpty()) {
            throw new IllegalArgumentException("员工不存在: " + username);
        }
        Long empId = Objects.requireNonNull(ou.get().getId(), "employeeId must not be null");
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(
                empId, PageRequest.of(page, size));
    }

    @Transactional
    public LeaveRequest cancelLeave(Long id, User currentUser) {
        final Long leaveId = Objects.requireNonNull(id, "leave id must not be null");
        final Long currentId = Objects.requireNonNull(currentUser.getId(), "currentUser.id must not be null");
        LeaveRequest lr = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave record not found: " + leaveId));

        if (!lr.getEmployeeId().equals(currentId)) {
            throw new SecurityException("Not authorized to cancel another user's leave record");
        }
        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Cancellation is only allowed when status is PENDING");
        }
        lr.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest saved = leaveRequestRepository.save(lr);

        AuditLog log = new AuditLog();
        log.setEntity("leave_requests");
        log.setEntityId(saved.getId());
        log.setAction("CANCEL");
        log.setUserId(currentId);
        log.setDetails(String.format("{\"employeeId\":%d,\"leaveId\":%d,\"status\":\"CANCELLED\"}",
                currentId, saved.getId()));
        auditLogRepository.save(log);

        return saved;
    }

    public Page<LeaveRequest> listPendingForManager(User manager, int page, int size) {
        if (manager.getRole() == null || manager.getRole() != UserRole.MANAGER) {
            throw new SecurityException("Only managers can view pending approvals");
        }
        Long managerId = Objects.requireNonNull(manager.getId(), "manager.id must not be null");
        List<Long> subordinateIds = userRepository.findSubordinateIdsByManagerId(managerId);
        if (subordinateIds == null || subordinateIds.isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }
        return leaveRequestRepository.findByEmployeeIdInAndStatusOrderByCreatedAtDesc(
                subordinateIds, LeaveStatus.PENDING, PageRequest.of(page, size));
    }

    public Page<LeaveRequest> listHandledByManager(User manager, int page, int size) {
        if (manager.getRole() == null || manager.getRole() != UserRole.MANAGER) {
            throw new SecurityException("Only managers can view approval history");
        }
        Long managerId = Objects.requireNonNull(manager.getId(), "manager.id must not be null");
        List<LeaveStatus> handled = Arrays.asList(LeaveStatus.APPROVED, LeaveStatus.REJECTED);
        return leaveRequestRepository.findByApproverIdAndStatusInOrderByUpdatedAtDesc(
                managerId, handled, PageRequest.of(page, size));
    }

    private void ensureManagerPermission(Long employeeId, User manager) {
        if (manager.getRole() == null || manager.getRole() != UserRole.MANAGER) {
            throw new SecurityException("Only managers can approve/reject");
        }
        Long managerId = Objects.requireNonNull(manager.getId(), "manager.id must not be null");
        List<Long> subordinateIds = userRepository.findSubordinateIdsByManagerId(managerId);
        if (subordinateIds == null || !subordinateIds.contains(employeeId)) {
            throw new SecurityException("Not authorized to act on non-subordinate employee leave");
        }
    }

    @Transactional
    public LeaveRequest approveLeave(Long id, String comment, User manager) {
        final Long leaveId = Objects.requireNonNull(id, "leave id must not be null");
        final Long managerId = Objects.requireNonNull(manager.getId(), "manager.id must not be null");
        LeaveRequest lr = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave record not found: " + leaveId));
        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Approval is only allowed when status is PENDING");
        }
        ensureManagerPermission(lr.getEmployeeId(), manager);

        lr.setStatus(LeaveStatus.APPROVED);
        lr.setApproverId(managerId);
        lr.setApprovalComment(comment);
        LeaveRequest saved = leaveRequestRepository.save(lr);

        AuditLog log = new AuditLog();
        log.setEntity("leave_requests");
        log.setEntityId(saved.getId());
        log.setAction("APPROVE");
        log.setUserId(managerId);
        log.setDetails(String.format("{\"employeeId\":%d,\"leaveId\":%d,\"comment\":%s,\"status\":\"APPROVED\"}",
                saved.getEmployeeId(), saved.getId(), jsonEscape(comment)));
        auditLogRepository.save(log);

        AuditLog notify = new AuditLog();
        notify.setEntity("notifications");
        notify.setEntityId(saved.getId());
        notify.setAction("UPDATE");
        notify.setUserId(managerId);
        notify.setDetails(String.format("{\"toUserId\":%d,\"message\":\"Your leave has been approved\"}", saved.getEmployeeId()));
        auditLogRepository.save(notify);

        // Email notification: notify employee after approval
        userRepository.findById(saved.getEmployeeId()).ifPresent(emp -> {
            String subject = "Leave approved";
            String text = String.format(
                    "Your leave request has been approved: %s %s ~ %s, %s day(s).\nApprover: %s, Comment: %s.\nID: %d, Status: APPROVED",
                    lr.getLeaveType().name(), lr.getStartDate(), lr.getEndDate(), lr.getDays(),
                    manager.getUsername(), comment != null ? comment : "none", saved.getId());
            mailService.send(emp.getEmail(), subject, text);
        });

        return saved;
    }

    @Transactional
    public LeaveRequest rejectLeave(Long id, String comment, User manager) {
        final Long leaveId = Objects.requireNonNull(id, "leave id must not be null");
        final Long managerId = Objects.requireNonNull(manager.getId(), "manager.id must not be null");
        LeaveRequest lr = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave record not found: " + leaveId));
        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Rejection is only allowed when status is PENDING");
        }
        ensureManagerPermission(lr.getEmployeeId(), manager);

        lr.setStatus(LeaveStatus.REJECTED);
        lr.setApproverId(managerId);
        lr.setApprovalComment(comment);
        LeaveRequest saved = leaveRequestRepository.save(lr);

        AuditLog log = new AuditLog();
        log.setEntity("leave_requests");
        log.setEntityId(saved.getId());
        log.setAction("REJECT");
        log.setUserId(managerId);
        log.setDetails(String.format("{\"employeeId\":%d,\"leaveId\":%d,\"comment\":%s,\"status\":\"REJECTED\"}",
                saved.getEmployeeId(), saved.getId(), jsonEscape(comment)));
        auditLogRepository.save(log);

        AuditLog notify = new AuditLog();
        notify.setEntity("notifications");
        notify.setEntityId(saved.getId());
        notify.setAction("UPDATE");
        notify.setUserId(managerId);
        notify.setDetails(String.format("{\"toUserId\":%d,\"message\":\"Your leave has been rejected\"}", saved.getEmployeeId()));
        auditLogRepository.save(notify);

        // Email notification: notify employee after rejection
        userRepository.findById(saved.getEmployeeId()).ifPresent(emp -> {
            String subject = "Leave rejected";
            String text = String.format(
                    "Your leave request was rejected: %s %s ~ %s, %s day(s).\nApprover: %s, Comment: %s.\nID: %d, Status: REJECTED",
                    lr.getLeaveType().name(), lr.getStartDate(), lr.getEndDate(), lr.getDays(),
                    manager.getUsername(), comment != null ? comment : "none", saved.getId());
            mailService.send(emp.getEmail(), subject, text);
        });

        return saved;
    }

    private String jsonEscape(String s) {
        if (s == null) return "null";
        return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
