package com.staffs.api.leave.api;

import com.staffs.api.leave.application.LeaveApplicationService;
import com.staffs.api.leave.infrastructure.LeaveRequestJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/leaves") @RequiredArgsConstructor
public class LeaveController {
    private final LeaveApplicationService service;

    // 1. Request leave (staff)
    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER','ADMIN')")
    public LeaveResponse request(@AuthenticationPrincipal String email,
                                 @RequestBody RequestLeaveCommand cmd) {
        var r = service.requestLeave(email, cmd.startDate(), cmd.endDate());
        return new LeaveResponse(r.getId(), r.getStaffId(), r.getStartDate(), r.getEndDate(), r.getStatus().name());
    }

    // 2. Cancel a leave request (approved or otherwise)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER','ADMIN')")
    public void cancel(@PathVariable String id) { service.cancel(id); }

    // 3. View my requests & statuses
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER','ADMIN')")
    public List<LeaveRequestJpa> my(@AuthenticationPrincipal String email) { return service.myRequests(email); }

    // 4. View remaining annual leave (business year starts Apr 1)
    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER','ADMIN')")
    public int myBalance(@AuthenticationPrincipal String email) { return service.remainingDays(email, LocalDate.now()); }
}

