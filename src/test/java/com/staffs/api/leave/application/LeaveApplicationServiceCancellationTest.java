package com.staffs.api.leave.application;

import com.staffs.api.leave.domain.model.LeaveStatus;
import com.staffs.api.leave.infrastructure.LeaveRequestJpa;
import com.staffs.api.leave.infrastructure.LeaveRequestRepository;
import com.staffs.api.leave.infrastructure.StaffJpa;
import com.staffs.api.leave.infrastructure.StaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveApplicationServiceCancellationTest {

    @Mock
    private LeaveRequestRepository leaveRepo;

    @Mock
    private StaffRepository staffRepo;

    @InjectMocks
    private LeaveApplicationService service;

    @Test
    void cancelApprovedRequest_refreshesRemainingBalanceWithLatestUsage() {
        var request = LeaveRequestJpa.builder()
                .id("req-1")
                .staffId("staff-1")
                .status(LeaveStatus.APPROVED)
                .startDate(LocalDate.of(2024, 4, 1))
                .endDate(LocalDate.of(2024, 4, 5))
                .build();
        when(leaveRepo.findById("req-1")).thenReturn(Optional.of(request));

        var staff = StaffJpa.builder()
                .id("staff-1")
                .annualLeaveAllocation(25)
                .leaveRemaining(10)
                .build();
        when(staffRepo.findById("staff-1")).thenReturn(Optional.of(staff));

        var otherApproved = List.of(
                LeaveRequestJpa.builder()
                        .id("req-2")
                        .staffId("staff-1")
                        .status(LeaveStatus.APPROVED)
                        .startDate(LocalDate.of(2024, 4, 10))
                        .endDate(LocalDate.of(2024, 4, 12))
                        .build(),
                LeaveRequestJpa.builder()
                        .id("req-3")
                        .staffId("staff-1")
                        .status(LeaveStatus.APPROVED)
                        .startDate(LocalDate.of(2024, 5, 1))
                        .endDate(LocalDate.of(2024, 5, 3))
                        .build()
        );
        when(leaveRepo.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                eq("staff-1"), eq(LeaveStatus.APPROVED), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(otherApproved);

        service.cancel("req-1");

        ArgumentCaptor<LeaveRequestJpa> leaveCaptor = ArgumentCaptor.forClass(LeaveRequestJpa.class);
        verify(leaveRepo).save(leaveCaptor.capture());
        assertEquals(LeaveStatus.CANCELLED, leaveCaptor.getValue().getStatus());

        ArgumentCaptor<StaffJpa> staffCaptor = ArgumentCaptor.forClass(StaffJpa.class);
        verify(staffRepo).save(staffCaptor.capture());
        assertEquals(19, staffCaptor.getValue().getLeaveRemaining());
    }

    @Test
    void cancelPendingRequest_doesNotRecalculateRemainingBalance() {
        var request = LeaveRequestJpa.builder()
                .id("req-1")
                .staffId("staff-1")
                .status(LeaveStatus.PENDING)
                .startDate(LocalDate.of(2024, 4, 1))
                .endDate(LocalDate.of(2024, 4, 5))
                .build();
        when(leaveRepo.findById("req-1")).thenReturn(Optional.of(request));

        service.cancel("req-1");

        verify(leaveRepo).save(request);
        verify(staffRepo, never()).save(any());
        verifyNoMoreInteractions(staffRepo);
    }
}
