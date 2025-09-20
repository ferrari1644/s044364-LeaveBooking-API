package com.staffs.api.leave.application;

import com.staffs.api.leave.infrastructure.StaffJpa;
import com.staffs.api.leave.infrastructure.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class StaffAdminService {
    private final StaffRepository staffRepo;

    public StaffJpa add(StaffJpa s) {
        if (s.getLeaveRemaining() == null && s.getAnnualLeaveAllocation() != null) {
            s.setLeaveRemaining(s.getAnnualLeaveAllocation());
        }
        return staffRepo.save(s);
    }
    public StaffJpa amend(String id, String department, Integer allocation, Integer leaveRemaining) {
        var e = staffRepo.findById(id).orElseThrow();
        if (department != null) e.setDepartment(department);
        if (allocation != null && allocation > 0) {
            int used = safe(e.getAnnualLeaveAllocation()) - safe(e.getLeaveRemaining());
            e.setAnnualLeaveAllocation(allocation);
            if (leaveRemaining == null) {
                e.setLeaveRemaining(Math.max(0, allocation - used));
            }
        }
        if (leaveRemaining != null) {
            int allocationValue = safe(e.getAnnualLeaveAllocation());
            int newBalance = Math.max(0, leaveRemaining);
            if (allocationValue > 0) newBalance = Math.min(newBalance, allocationValue);
            e.setLeaveRemaining(newBalance);
        }
        return staffRepo.save(e);
    }

    private int safe(Integer value) { return value == null ? 0 : value; }
}

