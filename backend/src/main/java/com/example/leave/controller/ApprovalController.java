package com.example.leave.controller;

import com.example.common.ApiResponse;
import com.example.common.PagedData;
import com.example.leave.dto.LeaveRequestDTO;
import com.example.leave.model.LeaveRequest;
import com.example.leave.model.User;
import com.example.leave.repository.UserRepository;
import com.example.leave.service.LeaveService;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/leave")
public class ApprovalController {

    private final LeaveService leaveService;
    private final UserRepository userRepository;

    public ApprovalController(LeaveService leaveService, UserRepository userRepository) {
        this.leaveService = leaveService;
        this.userRepository = userRepository;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PagedData<LeaveRequestDTO>>> pending(@RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size,
                                                                           Principal principal) {
        try {
            User manager = userRepository.findByUsername(principal.getName()).orElseThrow();
            Page<LeaveRequest> result = leaveService.listPendingForManager(manager, page, size);
            List<LeaveRequestDTO> items = result.getContent().stream()
                    .map(this::toDTO).collect(Collectors.toList());
            PagedData<LeaveRequestDTO> data = new PagedData<>(items, page, size, result.getTotalElements(), result.getTotalPages());
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(403, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(500, "服务器错误"));
        }
    }

    public static class ApproveRejectRequest {
        public String comment;
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> approve(@PathVariable("id") Long id,
                                                                @RequestBody ApproveRejectRequest body,
                                                                Principal principal) {
        try {
            User manager = userRepository.findByUsername(principal.getName()).orElseThrow();
            LeaveRequest saved = leaveService.approveLeave(id, body != null ? body.comment : null, manager);
            return ResponseEntity.ok(ApiResponse.success("审批通过", toDTO(saved)));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(500, "服务器错误"));
        }
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> reject(@PathVariable("id") Long id,
                                                               @RequestBody ApproveRejectRequest body,
                                                               Principal principal) {
        try {
            User manager = userRepository.findByUsername(principal.getName()).orElseThrow();
            LeaveRequest saved = leaveService.rejectLeave(id, body != null ? body.comment : null, manager);
            return ResponseEntity.ok(ApiResponse.success("审批拒绝", toDTO(saved)));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(500, "服务器错误"));
        }
    }

    private LeaveRequestDTO toDTO(LeaveRequest lr) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(lr.getId());
        dto.setEmployeeId(lr.getEmployeeId());
        dto.setLeaveType(lr.getLeaveType());
        dto.setStartDate(lr.getStartDate());
        dto.setEndDate(lr.getEndDate());
        dto.setDays(lr.getDays());
        dto.setComment(lr.getApprovalComment());
        dto.setStatus(lr.getStatus());
        dto.setCreatedAt(lr.getCreatedAt());
        dto.setUpdatedAt(lr.getUpdatedAt());
        return dto;
    }
}
