package com.example.leave.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 原生查询的接口投影，字段名与 SQL 中的别名一致。
 */
public interface LeaveExportRowProjection {
    Long getEmployeeId();
    String getEmployeeName();
    String getDepartment();
    String getLeaveType();
    LocalDate getStartDate();
    LocalDate getEndDate();
    BigDecimal getDays();
    String getStatus();
    String getApprover();
    String getApprovalComment();
}

