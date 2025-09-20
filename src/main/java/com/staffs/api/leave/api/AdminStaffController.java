package com.staffs.api.leave.api;

import com.staffs.api.leave.infrastructure.StaffJpa;
import com.staffs.api.leave.infrastructure.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class AdminStaffController {

    private final StaffRepository staffRepo;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public StaffJpa add(@RequestBody StaffJpa staff) {
        if (staff.getLeaveRemaining() == null && staff.getAnnualLeaveAllocation() != null) {
            staff.setLeaveRemaining(staff.getAnnualLeaveAllocation());
        }
        return staffRepo.save(staff);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StaffJpa update(@PathVariable String id, @RequestBody StaffJpa incoming) {
        var existing = staffRepo.findById(id).orElseThrow();
        if (incoming.getDepartment() != null) existing.setDepartment(incoming.getDepartment());
        Integer incomingAllocation = incoming.getAnnualLeaveAllocation();
        if (incomingAllocation != null && incomingAllocation > 0) {
            int used = safe(existing.getAnnualLeaveAllocation()) - safe(existing.getLeaveRemaining());
            existing.setAnnualLeaveAllocation(incomingAllocation);
            if (incoming.getLeaveRemaining() == null) {
                existing.setLeaveRemaining(Math.max(0, incomingAllocation - used));
            }
        }
        if (incoming.getLeaveRemaining() != null) {
            int allocation = safe(existing.getAnnualLeaveAllocation());
            int newBalance = Math.max(0, incoming.getLeaveRemaining());
            if (allocation > 0) {
                newBalance = Math.min(newBalance, allocation);
            }
            existing.setLeaveRemaining(newBalance);
        }
        return staffRepo.save(existing);
    }

    private int safe(Integer value) { return value == null ? 0 : value; }
}
