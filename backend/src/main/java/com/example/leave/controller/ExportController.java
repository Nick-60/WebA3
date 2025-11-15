package com.example.leave.controller;

import com.example.leave.service.ExportService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// no StreamingResponseBody to avoid client download issues; return bytes with content-length

@RestController
@RequestMapping("/api/leave/hr")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping(value = "/export")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Long departmentId,
                                         @RequestParam LocalDate from,
                                         @RequestParam LocalDate to) {
        String filename = buildFilename(departmentId, from, to);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(16 * 1024);
            exportService.exportHrReport(baos, departmentId, from, to);
            byte[] data = baos.toByteArray();
            MediaType xlsx = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            return ResponseEntity.ok()
                    .contentType(xlsx)
                    .header("Content-Disposition", contentDisposition(filename))
                    .contentLength(data.length)
                    .body(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildFilename(Long departmentId, LocalDate from, LocalDate to) {
        String dept = departmentId != null ? ("dept_" + departmentId) : "all";
        return String.format("leave_export_%s_%s_%s.xlsx", dept,
                from != null ? from : "from",
                to != null ? to : "to");
    }

    private static String contentDisposition(String filename) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        return "attachment; filename=" + encoded + "; filename*=UTF-8''" + encoded;
    }
}
