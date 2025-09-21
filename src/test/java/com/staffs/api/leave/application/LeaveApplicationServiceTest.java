package com.staffs.api.leave.application;

import com.staffs.api.leave.domain.model.LeaveStatus;
import com.staffs.api.leave.infrastructure.LeaveRequestJpa;
import com.staffs.api.leave.infrastructure.LeaveRequestRepository;
import com.staffs.api.leave.infrastructure.StaffJpa;
import com.staffs.api.leave.infrastructure.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveApplicationServiceTest {

    @Mock
    private LeaveRequestRepository leaveRepo;
    @Mock
    private StaffRepository staffRepo;

    private LeaveApplicationService service;

    @BeforeEach
    void setUp() {
        service = new LeaveApplicationService(leaveRepo, staffRepo);
    }

    @Test
    void requestLeave_savesPendingRequestForStaff() {
        var staff = StaffJpa.builder().id("alice@corp").build();
        when(staffRepo.findById("alice@corp")).thenReturn(Optional.of(staff));

        var start = LocalDate.of(2025, 5, 1);
        var end = LocalDate.of(2025, 5, 3);

        var result = service.requestLeave("alice@corp", start, end);

        assertThat(result.getStatus()).isEqualTo(LeaveStatus.PENDING);
        assertThat(result.getStaffId()).isEqualTo("alice@corp");

        var captor = ArgumentCaptor.forClass(LeaveRequestJpa.class);
        verify(leaveRepo).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getStaffId()).isEqualTo("alice@corp");
        assertThat(saved.getStartDate()).isEqualTo(start);
        assertThat(saved.getEndDate()).isEqualTo(end);
        assertThat(saved.getStatus()).isEqualTo(LeaveStatus.PENDING);
    }

    @Test
    void cancelApprovedRequest_marksCancelledAndRefreshesBalance() {
        var request = LeaveRequestJpa.builder()
                .id("req-1")
                .staffId("bob@corp")
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 2))
                .status(LeaveStatus.APPROVED)
                .build();
        when(leaveRepo.findById("req-1")).thenReturn(Optional.of(request));

        var staff = StaffJpa.builder()
                .id("bob@corp")
                .annualLeaveAllocation(20)
                .leaveRemaining(18)
                .build();
        when(staffRepo.findById("bob@corp")).thenReturn(Optional.of(staff));
        when(leaveRepo.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                eq("bob@corp"), eq(LeaveStatus.APPROVED), any(), any()))
                .thenReturn(List.of());

        service.cancel("req-1");

        assertThat(request.getStatus()).isEqualTo(LeaveStatus.CANCELLED);
        verify(leaveRepo).save(request);
        verify(staffRepo).save(staff);
        assertThat(staff.getLeaveRemaining()).isEqualTo(20);
    }

    @Test
    void cancelPendingRequest_doesNotTriggerBalanceRefresh() {
        var request = LeaveRequestJpa.builder()
                .id("req-2")
                .staffId("carol@corp")
                .status(LeaveStatus.PENDING)
                .build();
        when(leaveRepo.findById("req-2")).thenReturn(Optional.of(request));

        service.cancel("req-2");

        assertThat(request.getStatus()).isEqualTo(LeaveStatus.CANCELLED);
        verify(leaveRepo).save(request);
        verify(staffRepo, never()).findById(any());
    }

    @Test
    void myRequests_returnsRequestsSortedByCreatedDate() {
        when(leaveRepo.findByStaffIdOrderByCreatedAtDesc("dave@corp"))
                .thenReturn(List.of(mock(LeaveRequestJpa.class)));

        var result = service.myRequests("dave@corp");

        assertThat(result).hasSize(1);
        verify(leaveRepo).findByStaffIdOrderByCreatedAtDesc("dave@corp");
    }

    @Test
    void remainingDays_updatesStoredBalanceWhenChanged() {
        var staff = StaffJpa.builder()
                .id("erin@corp")
                .annualLeaveAllocation(20)
                .leaveRemaining(10)
                .build();
        when(staffRepo.findById("erin@corp")).thenReturn(Optional.of(staff));

        var approved = LeaveRequestJpa.builder()
                .startDate(LocalDate.of(2025, 4, 10))
                .endDate(LocalDate.of(2025, 4, 14))
                .status(LeaveStatus.APPROVED)
                .build();
        when(leaveRepo.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                eq("erin@corp"), eq(LeaveStatus.APPROVED), any(), any()))
                .thenReturn(List.of(approved));

        int remaining = service.remainingDays("erin@corp", LocalDate.of(2025, 8, 1));

        assertThat(remaining).isEqualTo(15);
        verify(staffRepo).save(staff);
        assertThat(staff.getLeaveRemaining()).isEqualTo(15);
    }

    @Test
    void remainingDays_doesNotPersistWhenBalanceMatches() {
        var staff = StaffJpa.builder()
                .id("frank@corp")
                .annualLeaveAllocation(25)
                .leaveRemaining(25)
                .build();
        when(staffRepo.findById("frank@corp")).thenReturn(Optional.of(staff));
        when(leaveRepo.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                eq("frank@corp"), eq(LeaveStatus.APPROVED), any(), any()))
                .thenReturn(List.of());

        int remaining = service.remainingDays("frank@corp", LocalDate.of(2025, 2, 1));

        assertThat(remaining).isEqualTo(25);
        verify(staffRepo, never()).save(any());
    }

    @Test
    void pendingForTeam_returnsEntireTeamWhenNoFilter() {
        when(staffRepo.findByManagerId("manager@corp")).thenReturn(List.of(
                StaffJpa.builder().id("s1").build(),
                StaffJpa.builder().id("s2").build()
        ));
        var req1 = LeaveRequestJpa.builder().id("r1").status(LeaveStatus.PENDING).build();
        var req2 = LeaveRequestJpa.builder().id("r2").status(LeaveStatus.PENDING).build();
        when(leaveRepo.findByStatusAndStaffIdIn(eq(LeaveStatus.PENDING), anyList()))
                .thenReturn(List.of(req1, req2));

        var results = service.pendingForTeam("manager@corp", null, null);

        assertThat(results).containsExactly(req1, req2);
        verify(leaveRepo).findByStatusAndStaffIdIn(eq(LeaveStatus.PENDING), eq(List.of("s1", "s2")));
    }

    @Test
    void pendingForTeam_appliesDateFilter() {
        when(staffRepo.findByManagerId("manager@corp")).thenReturn(List.of(
                StaffJpa.builder().id("s1").build()
        ));
        var inside = LeaveRequestJpa.builder()
                .id("inside")
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 5))
                .status(LeaveStatus.PENDING)
                .build();
        var outside = LeaveRequestJpa.builder()
                .id("outside")
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 3))
                .status(LeaveStatus.PENDING)
                .build();
        when(leaveRepo.findByStatusAndStaffIdIn(eq(LeaveStatus.PENDING), anyList()))
                .thenReturn(List.of(inside, outside));

        var results = service.pendingForTeam("manager@corp",
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31));

        assertThat(results).containsExactly(inside);
    }

    @Test
    void approve_doesNothingWhenAlreadyApproved() {
        var request = LeaveRequestJpa.builder()
                .id("req-approved")
                .status(LeaveStatus.APPROVED)
                .build();
        when(leaveRepo.findById("req-approved")).thenReturn(Optional.of(request));

        service.approve("req-approved");

        verify(leaveRepo, never()).save(any());
        verify(staffRepo, never()).findById(any());
    }

    @Test
    void reject_updatesStatusAndRefreshesIfPreviouslyApproved() {
        var request = LeaveRequestJpa.builder()
                .id("req-reject")
                .staffId("tom@corp")
                .status(LeaveStatus.APPROVED)
                .build();
        when(leaveRepo.findById("req-reject")).thenReturn(Optional.of(request));

        var staff = StaffJpa.builder()
                .id("tom@corp")
                .annualLeaveAllocation(10)
                .leaveRemaining(4)
                .build();
        when(staffRepo.findById("tom@corp")).thenReturn(Optional.of(staff));
        when(leaveRepo.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                eq("tom@corp"), eq(LeaveStatus.APPROVED), any(), any()))
                .thenReturn(List.of());

        service.reject("req-reject");

        assertThat(request.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        verify(leaveRepo).save(request);
        verify(staffRepo).save(staff);
    }

    @Test
    void reject_withoutPriorApprovalDoesNotRefreshBalance() {
        var request = LeaveRequestJpa.builder()
                .id("req-reject-2")
                .status(LeaveStatus.PENDING)
                .build();
        when(leaveRepo.findById("req-reject-2")).thenReturn(Optional.of(request));

        service.reject("req-reject-2");

        assertThat(request.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        verify(leaveRepo).save(request);
        verify(staffRepo, never()).findById(any());
    }

    @Test
    void remainingForStaff_delegatesToRemainingDays() {
        var staff = StaffJpa.builder()
                .id("uma@corp")
                .annualLeaveAllocation(15)
                .leaveRemaining(15)
                .build();
        when(staffRepo.findById("uma@corp")).thenReturn(Optional.of(staff));
        when(leaveRepo.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                eq("uma@corp"), eq(LeaveStatus.APPROVED), any(), any()))
                .thenReturn(List.of());

        int remaining = service.remainingForStaff("uma@corp", LocalDate.of(2025, 5, 5));

        assertThat(remaining).isEqualTo(15);
    }

    @Test
    void pendingByStaff_filtersToPendingOnly() {
        var pending = LeaveRequestJpa.builder().id("p").status(LeaveStatus.PENDING).build();
        var rejected = LeaveRequestJpa.builder().id("r").status(LeaveStatus.REJECTED).build();
        when(leaveRepo.findByStaffIdOrderByCreatedAtDesc("victor@corp"))
                .thenReturn(List.of(pending, rejected));

        var results = service.pendingByStaff("victor@corp");

        assertThat(results).containsExactly(pending);
    }

    @Test
    void pendingByManagerTeam_returnsPendingRequests() {
        when(staffRepo.findByManagerId("manager@corp")).thenReturn(List.of(
                StaffJpa.builder().id("s1").build(),
                StaffJpa.builder().id("s2").build()
        ));
        var pending = LeaveRequestJpa.builder().id("req").status(LeaveStatus.PENDING).build();
        when(leaveRepo.findByStatusAndStaffIdIn(LeaveStatus.PENDING, List.of("s1", "s2")))
                .thenReturn(List.of(pending));

        var results = service.pendingByManagerTeam("manager@corp");

        assertThat(results).containsExactly(pending);
    }

    @Test
    void pendingCompanyWide_returnsAllPending() {
        var pending = LeaveRequestJpa.builder().id("req").status(LeaveStatus.PENDING).build();
        when(leaveRepo.findByStatus(LeaveStatus.PENDING)).thenReturn(List.of(pending));

        var results = service.pendingCompanyWide();

        assertThat(results).containsExactly(pending);
    }

    @Test
    void adminApproveOnBehalf_delegatesToApprove() {
        var spyService = spy(new LeaveApplicationService(leaveRepo, staffRepo));
        doNothing().when(spyService).approve("req-123");

        spyService.adminApproveOnBehalf("req-123");

        verify(spyService).approve("req-123");
    }
}
