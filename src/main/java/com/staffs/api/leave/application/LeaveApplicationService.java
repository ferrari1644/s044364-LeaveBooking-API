package com.staffs.api.leave.application;

import com.staffs.api.leave.domain.model.LeaveRequest;
import com.staffs.api.leave.domain.model.LeaveStatus;
import com.staffs.api.leave.infrastructure.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class LeaveApplicationService {
    private final LeaveRequestRepository leaveRepo;
    private final StaffRepository staffRepo;

    // STAFF ACTION 1: Request leave (subject to manager approval)
    public LeaveRequest requestLeave(String staffId, LocalDate start, LocalDate end) {
        staffRepo.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        var req = LeaveRequest.createPending(staffId, start, end);
        leaveRepo.save(toJpa(req));
        return req;
    }

    // STAFF ACTION 2: Cancel a request (approved or otherwise)
    public void cancel(String requestId) {
        var r = leaveRepo.findById(requestId).orElseThrow(() -> new RuntimeException("Leave request not found"));
        var previousStatus = r.getStatus();
        r.setStatus(LeaveStatus.CANCELLED);
        leaveRepo.save(r);
        if (previousStatus == LeaveStatus.APPROVED) {
            refreshRemainingBalance(r.getStaffId());
        }
    }

    // STAFF ACTION 3: View my requests + status
    public List<LeaveRequestJpa> myRequests(String staffId) {
        return leaveRepo.findByStaffIdOrderByCreatedAtDesc(staffId);
    }

    // STAFF ACTION 4: Remaining days in current business year
    public int remainingDays(String staffId, LocalDate today) {
        var staff = staffRepo.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        int remaining = calculateRemainingDays(staff, today);
        updateStoredBalanceIfNeeded(staff, remaining);
        return remaining;
    }

    // MANAGER ACTION 1: View outstanding requests (team) with optional date filter
    public List<LeaveRequestJpa> pendingForTeam(String managerId, LocalDate from, LocalDate to) {
        var team = staffRepo.findByManagerId(managerId).stream().map(StaffJpa::getId).toList();
        var pending = leaveRepo.findByStatusAndStaffIdIn(LeaveStatus.PENDING, team);
        if (from == null && to == null) return pending;
        return pending.stream().filter(r -> inRange(r.getStartDate(), r.getEndDate(), from, to)).collect(Collectors.toList());
    }

    // MANAGER ACTION 2 & 3: Approve/Reject
    public void approve(String requestId) {
        var r = leaveRepo.findById(requestId).orElseThrow();
        if (r.getStatus() != LeaveStatus.APPROVED) {
            r.setStatus(LeaveStatus.APPROVED);
            leaveRepo.save(r);
            refreshRemainingBalance(r.getStaffId());
        }
    }
    public void reject(String requestId) {
        var r = leaveRepo.findById(requestId).orElseThrow();
        var previousStatus = r.getStatus();
        r.setStatus(LeaveStatus.REJECTED);
        leaveRepo.save(r);
        if (previousStatus == LeaveStatus.APPROVED) {
            refreshRemainingBalance(r.getStaffId());
        }
    }

    // MANAGER ACTION 4: View staff member remaining
    public int remainingForStaff(String staffId, LocalDate today) {
        return remainingDays(staffId, today);
    }

    // ADMIN ACTION 3: View outstanding requests by staff, team, or company
    public List<LeaveRequestJpa> pendingByStaff(String staffId) {
        return leaveRepo.findByStaffIdOrderByCreatedAtDesc(staffId).stream()
                .filter(r -> r.getStatus() == LeaveStatus.PENDING).toList();
    }
    public List<LeaveRequestJpa> pendingByManagerTeam(String managerId) {
        var team = staffRepo.findByManagerId(managerId).stream().map(StaffJpa::getId).toList();
        return leaveRepo.findByStatusAndStaffIdIn(LeaveStatus.PENDING, team);
    }
    public List<LeaveRequestJpa> pendingCompanyWide() {
        return leaveRepo.findByStatus(LeaveStatus.PENDING);
    }

    // ADMIN ACTION 5: Approve on behalf of manager
    public void adminApproveOnBehalf(String requestId) { approve(requestId); }

    private int daysInclusive(LocalDate s, LocalDate e) { return (int)(e.toEpochDay() - s.toEpochDay() + 1); }
    private boolean inRange(LocalDate s, LocalDate e, LocalDate from, LocalDate to) {
        if (from != null && e.isBefore(from)) return false;
        if (to != null && s.isAfter(to)) return false;
        return true;
    }
    private LeaveRequestJpa toJpa(LeaveRequest r) {
        return LeaveRequestJpa.builder()
                .id(r.getId()).staffId(r.getStaffId())
                .startDate(r.getStartDate()).endDate(r.getEndDate())
                .status(r.getStatus()).createdAt(r.getCreatedAt())
                .build();
    }

    private int calculateRemainingDays(StaffJpa staff, LocalDate today) {
        LocalDate ys = BusinessYear.start(today), ye = BusinessYear.end(today);
        var approved = leaveRepo.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                staff.getId(), LeaveStatus.APPROVED, ys, ye);
        int used = approved.stream().mapToInt(l -> daysInclusive(l.getStartDate(), l.getEndDate())).sum();
        int allocation = Optional.ofNullable(staff.getAnnualLeaveAllocation()).orElse(0);
        return Math.max(0, allocation - used);
    }

    private void refreshRemainingBalance(String staffId) {
        var staff = staffRepo.findById(staffId).orElseThrow(() -> new RuntimeException("Staff not found"));
        int remaining = calculateRemainingDays(staff, LocalDate.now());
        updateStoredBalanceIfNeeded(staff, remaining);
    }

    private void updateStoredBalanceIfNeeded(StaffJpa staff, int remaining) {
        if (!Objects.equals(staff.getLeaveRemaining(), remaining)) {
            staff.setLeaveRemaining(remaining);
            staffRepo.save(staff);
        }
    }
}

