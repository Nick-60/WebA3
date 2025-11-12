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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        AuditLogRepository auditLogRepository,
                        UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public LeaveRequest createLeave(User currentUser, LeaveType leaveType, LocalDate start, LocalDate end, String comment) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start_date 与 end_date 不能为空");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end_date 必须不早于 start_date");
        }

        List<LeaveStatus> activeStatuses = Arrays.asList(LeaveStatus.PENDING, LeaveStatus.APPROVED);
        boolean overlap = leaveRequestRepository
                .existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        currentUser.getId(), activeStatuses, end, start);
        if (overlap) {
            throw new IllegalArgumentException("申请日期与未结束的请假记录重叠");
        }

        long daysCount = ChronoUnit.DAYS.between(start, end) + 1;
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId(currentUser.getId());
        lr.setLeaveType(leaveType);
        lr.setStartDate(start);
        lr.setEndDate(end);
        lr.setDays(BigDecimal.valueOf(daysCount));
        lr.setStatus(LeaveStatus.PENDING);
        // comment 存到 approvalComment 作为备注字段（表结构无单独 comment 字段）
        lr.setApprovalComment(comment);

        LeaveRequest saved = leaveRequestRepository.save(lr);

        AuditLog log = new AuditLog();
        log.setEntity("leave_requests");
        log.setEntityId(saved.getId());
        log.setAction("CREATE");
        log.setUserId(currentUser.getId());
        log.setDetails(String.format("{\"employeeId\":%d,\"range\":\"%s~%s\",\"days\":%d}",
                currentUser.getId(), start, end, daysCount));
        auditLogRepository.save(log);

        return saved;
    }

    public Page<LeaveRequest> listEmployeeLeaves(String username, int page, int size) {
        Optional<User> ou = userRepository.findByUsername(username);
        if (ou.isEmpty()) {
            throw new IllegalArgumentException("员工不存在: " + username);
        }
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(
                ou.get().getId(), PageRequest.of(page, size));
    }

    @Transactional
    public LeaveRequest cancelLeave(Long id, User currentUser) {
        LeaveRequest lr = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("请假记录不存在: " + id));

        if (!lr.getEmployeeId().equals(currentUser.getId())) {
            throw new SecurityException("无权撤销他人请假记录");
        }
        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("仅当状态为 PENDING 时可撤销");
        }
        lr.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest saved = leaveRequestRepository.save(lr);

        AuditLog log = new AuditLog();
        log.setEntity("leave_requests");
        log.setEntityId(saved.getId());
        log.setAction("CANCEL");
        log.setUserId(currentUser.getId());
        log.setDetails(String.format("{\"employeeId\":%d,\"leaveId\":%d,\"status\":\"CANCELLED\"}",
                currentUser.getId(), saved.getId()));
        auditLogRepository.save(log);

        return saved;
    }

    public Page<LeaveRequest> listPendingForManager(User manager, int page, int size) {
        if (manager.getRole() == null || manager.getRole() != UserRole.MANAGER) {
            throw new SecurityException("仅经理可查看待审批列表");
        }
        List<Long> subordinateIds = userRepository.findSubordinateIdsByManagerId(manager.getId());
        if (subordinateIds == null || subordinateIds.isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }
        return leaveRequestRepository.findByEmployeeIdInAndStatusOrderByCreatedAtDesc(
                subordinateIds, LeaveStatus.PENDING, PageRequest.of(page, size));
    }

    private void ensureManagerPermission(Long employeeId, User manager) {
        if (manager.getRole() == null || manager.getRole() != UserRole.MANAGER) {
            throw new SecurityException("仅经理可审批");
        }
        List<Long> subordinateIds = userRepository.findSubordinateIdsByManagerId(manager.getId());
        if (subordinateIds == null || !subordinateIds.contains(employeeId)) {
            throw new SecurityException("无权审批非直属部门员工的请假");
        }
    }

    @Transactional
    public LeaveRequest approveLeave(Long id, String comment, User manager) {
        LeaveRequest lr = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("请假记录不存在: " + id));
        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("仅当状态为 PENDING 时可审批通过");
        }
        ensureManagerPermission(lr.getEmployeeId(), manager);

        lr.setStatus(LeaveStatus.APPROVED);
        lr.setApproverId(manager.getId());
        lr.setApprovalComment(comment);
        LeaveRequest saved = leaveRequestRepository.save(lr);

        AuditLog log = new AuditLog();
        log.setEntity("leave_requests");
        log.setEntityId(saved.getId());
        log.setAction("APPROVE");
        log.setUserId(manager.getId());
        log.setDetails(String.format("{\"employeeId\":%d,\"leaveId\":%d,\"comment\":%s,\"status\":\"APPROVED\"}",
                saved.getEmployeeId(), saved.getId(), jsonEscape(comment)));
        auditLogRepository.save(log);

        AuditLog notify = new AuditLog();
        notify.setEntity("notifications");
        notify.setEntityId(saved.getId());
        notify.setAction("UPDATE");
        notify.setUserId(manager.getId());
        notify.setDetails(String.format("{\"toUserId\":%d,\"message\":\"Your leave has been approved\"}", saved.getEmployeeId()));
        auditLogRepository.save(notify);

        return saved;
    }

    @Transactional
    public LeaveRequest rejectLeave(Long id, String comment, User manager) {
        LeaveRequest lr = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("请假记录不存在: " + id));
        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("仅当状态为 PENDING 时可拒绝");
        }
        ensureManagerPermission(lr.getEmployeeId(), manager);

        lr.setStatus(LeaveStatus.REJECTED);
        lr.setApproverId(manager.getId());
        lr.setApprovalComment(comment);
        LeaveRequest saved = leaveRequestRepository.save(lr);

        AuditLog log = new AuditLog();
        log.setEntity("leave_requests");
        log.setEntityId(saved.getId());
        log.setAction("REJECT");
        log.setUserId(manager.getId());
        log.setDetails(String.format("{\"employeeId\":%d,\"leaveId\":%d,\"comment\":%s,\"status\":\"REJECTED\"}",
                saved.getEmployeeId(), saved.getId(), jsonEscape(comment)));
        auditLogRepository.save(log);

        AuditLog notify = new AuditLog();
        notify.setEntity("notifications");
        notify.setEntityId(saved.getId());
        notify.setAction("UPDATE");
        notify.setUserId(manager.getId());
        notify.setDetails(String.format("{\"toUserId\":%d,\"message\":\"Your leave has been rejected\"}", saved.getEmployeeId()));
        auditLogRepository.save(notify);

        return saved;
    }

    private String jsonEscape(String s) {
        if (s == null) return "null";
        return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
