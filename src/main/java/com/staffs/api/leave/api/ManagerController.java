package com.staffs.api.leave.api;

import com.staffs.api.leave.application.LeaveApplicationService;
import com.staffs.api.leave.infrastructure.LeaveRequestJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/manager") @RequiredArgsConstructor
public class ManagerController {
    private final LeaveApplicationService service;

    // 1. View outstanding requests for own team, with optional date filters
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public List<LeaveRequestJpa> pending(
            @AuthenticationPrincipal String managerEmail,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.pendingForTeam(managerEmail, from, to);
    }

    // 2. Approve a request
    @PutMapping("/leaves/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public void approve(@PathVariable String id) { service.approve(id); }

    // 3. Reject a request
    @PutMapping("/leaves/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public void reject(@PathVariable String id) { service.reject(id); }

    // 4. View remaining days for a member of staff
    @GetMapping("/staff/{staffId}/balance")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public int staffBalance(@PathVariable String staffId) {
        return service.remainingForStaff(staffId, LocalDate.now());
    }
}

