package com.staffs.api.leave.api;

import com.staffs.api.leave.domain.model.LeaveStatus;
import com.staffs.api.leave.infrastructure.LeaveRequestRepository;
import com.staffs.api.leave.infrastructure.StaffJpa;
import com.staffs.api.leave.infrastructure.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/analytics") @RequiredArgsConstructor
public class AnalyticsController {
    private final LeaveRequestRepository leaveRepo;
    private final StaffRepository staffRepo;

    // Company pending count
    @GetMapping("/pending/company")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String,Integer> companyPending() {
        return Map.of("pending", leaveRepo.findByStatus(LeaveStatus.PENDING).size());
    }

    // Usage by staff (approved days within business year)
    @GetMapping("/usage/{staffId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Map<String,Object> usage(@PathVariable String staffId) {
        var today = LocalDate.now();
        var ys = com.staffs.api.leave.application.BusinessYear.start(today);
        var ye = com.staffs.api.leave.application.BusinessYear.end(today);
        var approved = leaveRepo.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                staffId, LeaveStatus.APPROVED, ys, ye);
        int used = approved.stream().mapToInt(l -> (int)(l.getEndDate().toEpochDay()-l.getStartDate().toEpochDay()+1)).sum();
        return Map.of("staffId", staffId, "businessYearStart", ys, "businessYearEnd", ye, "approvedDaysUsed", used);
    }

    // Manager team snapshot: pending & usage per staff
    @GetMapping("/team/{managerId}/snapshot")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Map<String,Object> teamSnapshot(@PathVariable String managerId) {
        var team = staffRepo.findByManagerId(managerId);
        var pending = leaveRepo.findByStatusAndStaffIdIn(LeaveStatus.PENDING,
                team.stream().map(StaffJpa::getId).toList());
        var today = LocalDate.now();
        var ys = com.staffs.api.leave.application.BusinessYear.start(today);
        var ye = com.staffs.api.leave.application.BusinessYear.end(today);

        Map<String,Integer> usedByStaff = new HashMap<>();
        for (var s : team) {
            var approved = leaveRepo.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                    s.getId(), LeaveStatus.APPROVED, ys, ye);
            int used = approved.stream().mapToInt(l -> (int)(l.getEndDate().toEpochDay() - l.getStartDate().toEpochDay() + 1)).sum();
            usedByStaff.put(s.getId(), used);
        }
        return Map.of(
                "teamSize", team.size(),
                "pendingCount", pending.size(),
                "approvedDaysUsedByStaff", usedByStaff
        );
    }
}

