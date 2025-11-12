package com.example.leave.controller;

import com.example.common.ApiResponse;
import com.example.common.PagedData;
import com.example.leave.dto.LeaveRequestCreateDTO;
import com.example.leave.dto.LeaveRequestDTO;
import com.example.leave.model.LeaveRequest;
import com.example.leave.model.User;
import com.example.leave.model.UserRole;
import com.example.leave.repository.UserRepository;
import com.example.leave.service.LeaveService;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/leave")
public class LeaveRequestController {
    private final LeaveService leaveService;
    private final UserRepository userRepository;

    @Autowired
    public LeaveRequestController(LeaveService leaveService, UserRepository userRepository) {
        this.leaveService = leaveService;
        this.userRepository = userRepository;
    }

    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> request(@RequestBody LeaveRequestCreateDTO body,
                                                                Principal principal) {
        try {
            User current = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("当前用户不存在"));

            LeaveRequest saved = leaveService.createLeave(
                    current, body.getLeaveType(), body.getStartDate(), body.getEndDate(), body.getComment());

            LeaveRequestDTO dto = toDTO(saved);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("创建成功", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "服务器错误"));
        }
    }

    @GetMapping("/employee/{empId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN','HR','MANAGER')")
    public ResponseEntity<ApiResponse<PagedData<LeaveRequestDTO>>> list(@PathVariable("empId") String empId,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                        Principal principal) {
        try {
            User current = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("当前用户不存在"));
            // 简单授权：仅本人或 ADMIN 可查看
            if (!current.getUsername().equals(empId)) {
                UserRole role = current.getRole();
                if (role == null || role != UserRole.ADMIN) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error(403, "无权查看他人请假记录"));
                }
            }

            Page<LeaveRequest> result = leaveService.listEmployeeLeaves(empId, page, size);
            List<LeaveRequestDTO> items = result.getContent().stream()
                    .map(this::toDTO).collect(Collectors.toList());
            PagedData<LeaveRequestDTO> data = new PagedData<>(items, page, size, result.getTotalElements(), result.getTotalPages());
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "服务器错误"));
        }
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> cancel(@PathVariable("id") Long id,
                                                               Principal principal) {
        try {
            User current = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("当前用户不存在"));
            LeaveRequest saved = leaveService.cancelLeave(id, current);
            return ResponseEntity.ok(ApiResponse.success("撤销成功", toDTO(saved)));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "服务器错误"));
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
