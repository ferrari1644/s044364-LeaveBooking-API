package com.staffs.api.leave.api;

public record AmendStaffRequest(String department, Integer annualLeaveAllocation, Integer leaveRemaining) {
}
