package com.staffs.api.leave.api;

import com.staffs.api.leave.application.LeaveApplicationService;
import com.staffs.api.leave.application.StaffAdminService;
import com.staffs.api.leave.infrastructure.LeaveRequestJpa;
import com.staffs.api.leave.infrastructure.StaffJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminController {
    private final StaffAdminService staffAdmin;
    private final LeaveApplicationService leaves;

    // 1. Add a new member of staff
    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public StaffJpa add(@RequestBody StaffJpa s) { return staffAdmin.add(s); }

    // 2. Amend role/department (role changes would happen in identity; here dept/allocation)
    @PutMapping("/staff/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StaffJpa amend(@PathVariable String id, @RequestBody AmendStaffRequest req) {
        return staffAdmin.amend(id, req.department(), req.annualLeaveAllocation(), req.leaveRemaining());
    }

    // 3. View outstanding leave requests filtered by staff, manager’s team, or company
    @GetMapping("/pending/staff/{staffId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<LeaveRequestJpa> pendingByStaff(@PathVariable String staffId) { return leaves.pendingByStaff(staffId); }

    @GetMapping("/pending/team/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<LeaveRequestJpa> pendingByTeam(@PathVariable String managerId) { return leaves.pendingByManagerTeam(managerId); }

    @GetMapping("/pending/company")
    @PreAuthorize("hasRole('ADMIN')")
    public List<LeaveRequestJpa> pendingCompany() { return leaves.pendingCompanyWide(); }

    // 4. Amend amount of annual leave assigned to a member of staff → reuse amend endpoint above

    // 5. Approve on behalf (system-wide usage tracking comes in analytics section)
    @PutMapping("/leaves/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public void approveOnBehalf(@PathVariable String id) { leaves.adminApproveOnBehalf(id); }
}

