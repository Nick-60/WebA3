package com.example.leave.repository;

import com.example.leave.model.LeaveRequest;
import com.example.leave.model.LeaveStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Page<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);

    boolean existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId, List<LeaveStatus> statuses, LocalDate newEnd, LocalDate newStart);

    Page<LeaveRequest> findByEmployeeIdInAndStatusOrderByCreatedAtDesc(List<Long> employeeIds,
                                                                       LeaveStatus status,
                                                                       Pageable pageable);

    Page<LeaveRequest> findByApproverIdAndStatusInOrderByUpdatedAtDesc(Long approverId,
                                                                        List<LeaveStatus> statuses,
                                                                        Pageable pageable);

    /**
     * 导出报表用的分页查询（原生 SQL，联表获取员工、部门、审批人等字段）。
     * 使用 Pageable 生成 LIMIT/OFFSET，返回接口投影以便流式写入 Excel。
     */
    @Query(value = """
        SELECT
            lr.employee_id AS employeeId,
            e.username     AS employeeName,
            COALESCE(d.name, '') AS department,
            lr.leave_type  AS leaveType,
            lr.start_date  AS startDate,
            lr.end_date    AS endDate,
            lr.days        AS days,
            lr.status      AS status,
            a.username     AS approver,
            lr.employee_comment AS employeeComment,
            lr.approval_comment AS approvalComment
        FROM leave_requests lr
        JOIN users e ON e.id = lr.employee_id
        LEFT JOIN departments d ON d.id = e.department_id
        LEFT JOIN users a ON a.id = lr.approver_id
        WHERE (:departmentId IS NULL OR e.department_id = :departmentId)
          AND lr.start_date >= :from
          AND lr.end_date   <= :to
        ORDER BY lr.created_at DESC
        """,
        nativeQuery = true)
    List<LeaveExportRowProjection> findExportRows(@Param("departmentId") Long departmentId,
                                                  @Param("from") LocalDate from,
                                                  @Param("to") LocalDate to,
                                                  Pageable pageable);
}
