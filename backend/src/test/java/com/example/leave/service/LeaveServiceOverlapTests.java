package com.example.leave.service;

import com.example.leave.model.LeaveRequest;
import com.example.leave.model.LeaveType;
import com.example.leave.model.User;
import com.example.leave.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class LeaveServiceOverlapTests {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void overlapShouldThrow() {
        User emp = userRepository.findByUsername("emp").orElseThrow();
        // 先创建一条待审批的请假 2025-01-10 ~ 2025-01-12
        LeaveRequest lr1 = leaveService.createLeave(emp, LeaveType.ANNUAL,
                LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 12), "first");
        assertNotNull(lr1.getId());

        // 创建与其重叠的申请 2025-01-11 ~ 2025-01-13，应抛出异常
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                leaveService.createLeave(emp, LeaveType.SICK,
                        LocalDate.of(2025, 1, 11), LocalDate.of(2025, 1, 13), "overlap"));
        assertTrue(ex.getMessage().contains("重叠"));
    }

    @Test
    @Transactional
    void nonOverlapShouldCreate() {
        User emp = userRepository.findByUsername("emp").orElseThrow();
        // 创建一条 2025-02-01 ~ 2025-02-02
        LeaveRequest lr1 = leaveService.createLeave(emp, LeaveType.ANNUAL,
                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 2), "first");
        assertNotNull(lr1.getId());

        // 创建不重叠的 2025-02-10 ~ 2025-02-11，应成功
        LeaveRequest lr2 = leaveService.createLeave(emp, LeaveType.SICK,
                LocalDate.of(2025, 2, 10), LocalDate.of(2025, 2, 11), "no-overlap");
        assertNotNull(lr2.getId());
    }
}

