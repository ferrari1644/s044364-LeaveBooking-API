package com.staffs.api.leave.application;

import com.staffs.api.leave.infrastructure.StaffJpa;
import com.staffs.api.leave.infrastructure.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class StaffAdminService {
    private final StaffRepository staffRepo;

    public StaffJpa add(StaffJpa s) { return staffRepo.save(s); }
    public StaffJpa amend(String id, String department, Integer allocation) {
        var e = staffRepo.findById(id).orElseThrow();
        if (department != null) e.setDepartment(department);
        if (allocation != null && allocation > 0) e.setAnnualLeaveAllocation(allocation);
        return staffRepo.save(e);
    }
}

