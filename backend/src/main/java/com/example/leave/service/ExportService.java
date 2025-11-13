package com.example.leave.service;

import com.example.leave.repository.LeaveExportRowProjection;
import com.example.leave.repository.LeaveRequestRepository;
import java.io.OutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ExportService {

    private final LeaveRequestRepository leaveRequestRepository;

    public ExportService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    /**
     * 按部门与时间范围导出 HR 报表（流式），写入给定输出流。
     */
    public void exportHrReport(OutputStream out,
                               Long departmentId,
                               LocalDate from,
                               LocalDate to) throws IOException {
        // 每次仅保留最近 100 行在内存中，其余写入临时文件以降低内存占用
        try (SXSSFWorkbook wb = new SXSSFWorkbook(100)) {
            Sheet sheet = wb.createSheet("HR Export");
            int rowIdx = 0;
            // Header
            Row header = sheet.createRow(rowIdx++);
            String[] heads = new String[]{
                "employee_id","employee_name","department","leave_type",
                "start_date","end_date","days","status","approver",
                "employee_comment","approval_comment"
            };
            for (int i = 0; i < heads.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(heads[i]);
            }

            final int pageSize = 1000;
            int page = 0;
            while (true) {
                List<LeaveExportRowProjection> rows = leaveRequestRepository
                        .findExportRows(departmentId, from, to, PageRequest.of(page, pageSize));
                if (rows.isEmpty()) {
                    break;
                }
                for (LeaveExportRowProjection r : rows) {
                    Row row = sheet.createRow(rowIdx++);
                    int col = 0;
                    setString(row, col++, r.getEmployeeId() != null ? String.valueOf(r.getEmployeeId()) : "");
                    setString(row, col++, safe(r.getEmployeeName()));
                    setString(row, col++, safe(r.getDepartment()));
                    setString(row, col++, safe(r.getLeaveType()));
                    setString(row, col++, r.getStartDate() != null ? r.getStartDate().toString() : "");
                    setString(row, col++, r.getEndDate() != null ? r.getEndDate().toString() : "");
                    setString(row, col++, toString(r.getDays()));
                    setString(row, col++, safe(r.getStatus()));
                    setString(row, col++, safe(r.getApprover()));
                    setString(row, col++, safe(r.getEmployeeComment()));
                    setString(row, col++, safe(r.getApprovalComment()));
                }
                if (rows.size() < pageSize) {
                    break; // 最后一页
                }
                page++;
            }

            wb.write(out);
            wb.dispose();
        }
    }

    private static void setString(Row row, int idx, String val) {
        Cell c = row.createCell(idx);
        c.setCellValue(val != null ? val : "");
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String toString(BigDecimal bd) {
        return bd == null ? "" : bd.stripTrailingZeros().toPlainString();
    }
}
