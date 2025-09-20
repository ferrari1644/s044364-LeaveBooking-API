package com.staffs.api.leave.application;

import com.staffs.api.leave.domain.model.LeaveStatus;
import com.staffs.api.leave.infrastructure.LeaveRequestJpa;
import com.staffs.api.leave.infrastructure.LeaveRequestRepository;
import com.staffs.api.leave.infrastructure.StaffJpa;
import com.staffs.api.leave.infrastructure.StaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveApplicationServiceBalanceTest {

    @Mock
    private LeaveRequestRepository leaveRepo;

    @Mock
    private StaffRepository staffRepo;

    @InjectMocks
    private LeaveApplicationService service;

    @Test
    void remainingDays_updatesStoredBalanceWhenOutOfSync() {
        var today = LocalDate.of(2024, 6, 15);
        var staff = StaffJpa.builder()
                .id("staff-1")
                .annualLeaveAllocation(20)
                .leaveRemaining(2)
                .build();
        when(staffRepo.findById("staff-1")).thenReturn(Optional.of(staff));

        var approved = List.of(
                LeaveRequestJpa.builder().startDate(LocalDate.of(2024, 4, 1)).endDate(LocalDate.of(2024, 4, 5)).status(LeaveStatus.APPROVED).build(),
                LeaveRequestJpa.builder().startDate(LocalDate.of(2024, 5, 1)).endDate(LocalDate.of(2024, 5, 3)).status(LeaveStatus.APPROVED).build()
        );
        var yearStart = BusinessYear.start(today);
        var yearEnd = BusinessYear.end(today);
        when(leaveRepo.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                "staff-1", LeaveStatus.APPROVED, yearStart, yearEnd)).thenReturn(approved);

        int remaining = service.remainingDays("staff-1", today);

        assertEquals(12, remaining);
        verify(staffRepo).save(staff);
        assertEquals(12, staff.getLeaveRemaining());
    }

    @Test
    void remainingDays_doesNotPersistWhenAlreadyCurrent() {
        var today = LocalDate.of(2024, 6, 15);
        var staff = StaffJpa.builder()
                .id("staff-1")
                .annualLeaveAllocation(15)
                .leaveRemaining(10)
                .build();
        when(staffRepo.findById("staff-1")).thenReturn(Optional.of(staff));

        var approved = List.of(
                LeaveRequestJpa.builder().startDate(LocalDate.of(2024, 4, 1)).endDate(LocalDate.of(2024, 4, 3)).status(LeaveStatus.APPROVED).build(),
                LeaveRequestJpa.builder().startDate(LocalDate.of(2024, 4, 10)).endDate(LocalDate.of(2024, 4, 10)).status(LeaveStatus.APPROVED).build()
        );
        var yearStart = BusinessYear.start(today);
        var yearEnd = BusinessYear.end(today);
        when(leaveRepo.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                "staff-1", LeaveStatus.APPROVED, yearStart, yearEnd)).thenReturn(approved);

        int remaining = service.remainingDays("staff-1", today);

        assertEquals(10, remaining);
        verify(staffRepo, never()).save(staff);
    }
}
