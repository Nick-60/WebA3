package com.example.leave.export;

import com.example.leave.repository.LeaveExportRowProjection;
import com.example.leave.repository.LeaveRequestRepository;
import com.example.leave.service.ExportService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class ExportServiceBigDataTests {
  static class Row implements LeaveExportRowProjection {
    Long employeeId; String employeeName; String department; String leaveType;
    LocalDate startDate; LocalDate endDate; BigDecimal days;
    String status; String approver; String employeeComment; String approvalComment;
    Row(long id, LocalDate s, LocalDate e) {
      employeeId = id;
      employeeName = "emp-" + id;
      department = "Engineering";
      leaveType = "ANNUAL";
      startDate = s;
      endDate = e;
      days = BigDecimal.ONE;
      status = "APPROVED";
      approver = "mgr";
      employeeComment = "";
      approvalComment = "";
    }
    public Long getEmployeeId(){return employeeId;}
    public String getEmployeeName(){return employeeName;}
    public String getDepartment(){return department;}
    public String getLeaveType(){return leaveType;}
    public LocalDate getStartDate(){return startDate;}
    public LocalDate getEndDate(){return endDate;}
    public BigDecimal getDays(){return days;}
    public String getStatus(){return status;}
    public String getApprover(){return approver;}
    public String getEmployeeComment(){return employeeComment;}
    public String getApprovalComment(){return approvalComment;}
  }

  @Test
  void export_100k_rows_without_db() throws Exception {
    int total = 100_000;
    LeaveRequestRepository repo = Mockito.mock(LeaveRequestRepository.class);
    Mockito.when(repo.findExportRows(any(), any(), any(), any(Pageable.class))).thenAnswer(inv -> {
      Pageable p = inv.getArgument(3, Pageable.class);
      int page = p.getPageNumber();
      int size = p.getPageSize();
      int start = page * size;
      int end = Math.min(start + size, total);
      int count = Math.max(end - start, 0);
      List<LeaveExportRowProjection> rows = new ArrayList<>(count);
      for (int i = start; i < end; i++) {
        long id = i + 1L;
        LocalDate s = LocalDate.of(2024,1,1).plusDays(i % 30);
        rows.add(new Row(id, s, s.plusDays(1)));
      }
      return rows;
    });

    ExportService svc = new ExportService(repo);
    Path tmp = Path.of("target", "hr-export-100k.xlsx");
    Files.deleteIfExists(tmp);
    try (OutputStream out = Files.newOutputStream(tmp)) {
      svc.exportHrReport(out, null, LocalDate.of(2024,1,1), LocalDate.of(2024,12,31));
    }
    long fileSize = Files.size(tmp);
    System.out.println("xlsx path: " + tmp.toAbsolutePath());
    System.out.println("xlsx size: " + fileSize);
    assertTrue(fileSize > 1_000_000);
  }
}