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
        return staffRepo.save(staff);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StaffJpa update(@PathVariable String id, @RequestBody StaffJpa incoming) {
        var existing = staffRepo.findById(id).orElseThrow();
        if (incoming.getDepartment() != null) existing.setDepartment(incoming.getDepartment());
        if (incoming.getAnnualLeaveAllocation() > 0) existing.setAnnualLeaveAllocation(incoming.getAnnualLeaveAllocation());
        return staffRepo.save(existing);
    }
}
