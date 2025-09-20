package com.staffs.api.leave.api;

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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private AnalyticsController controller;

    @Test
    void companyPendingCountsPendingRequests() {
        when(leaveRequestRepository.findByStatus(LeaveStatus.PENDING))
                .thenReturn(List.of(LeaveRequestJpa.builder().id("1").build(), LeaveRequestJpa.builder().id("2").build()));

        Map<String, Integer> result = controller.companyPending();

        assertThat(result).containsEntry("pending", 2);
        verify(leaveRequestRepository).findByStatus(LeaveStatus.PENDING);
    }

    @Test
    void usageSummarisesApprovedDaysWithinBusinessYear() {
        LocalDate today = LocalDate.now();
        LocalDate yearStart = com.staffs.api.leave.application.BusinessYear.start(today);
        LocalDate yearEnd = com.staffs.api.leave.application.BusinessYear.end(today);
        var approved = List.of(
                LeaveRequestJpa.builder()
                        .id("1")
                        .staffId("staff")
                        .status(LeaveStatus.APPROVED)
                        .startDate(yearStart)
                        .endDate(yearStart.plusDays(2))
                        .build(),
                LeaveRequestJpa.builder()
                        .id("2")
                        .staffId("staff")
                        .status(LeaveStatus.APPROVED)
                        .startDate(yearStart.plusDays(10))
                        .endDate(yearStart.plusDays(10))
                        .build()
        );
        when(leaveRequestRepository.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                "staff", LeaveStatus.APPROVED, yearStart, yearEnd)).thenReturn(approved);

        Map<String, Object> usage = controller.usage("staff");

        assertThat(usage).containsEntry("staffId", "staff");
        assertThat(usage).containsEntry("approvedDaysUsed", 4);
        assertThat(usage).containsEntry("businessYearStart", yearStart);
        assertThat(usage).containsEntry("businessYearEnd", yearEnd);
    }

    @Test
    void teamSnapshotCombinesPendingCountsAndUsageByStaff() {
        LocalDate today = LocalDate.now();
        LocalDate yearStart = com.staffs.api.leave.application.BusinessYear.start(today);
        LocalDate yearEnd = com.staffs.api.leave.application.BusinessYear.end(today);
        var staffMembers = List.of(
                StaffJpa.builder().id("s1").fullName("Staff One").annualLeaveAllocation(20).leaveRemaining(10).build(),
                StaffJpa.builder().id("s2").fullName("Staff Two").annualLeaveAllocation(25).leaveRemaining(12).build()
        );
        when(staffRepository.findByManagerId("manager")).thenReturn(staffMembers);
        when(leaveRequestRepository.findByStatusAndStaffIdIn(eq(LeaveStatus.PENDING), anyList()))
                .thenReturn(List.of(LeaveRequestJpa.builder().id("p1").build()));
        when(leaveRequestRepository.findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                anyString(), eq(LeaveStatus.APPROVED), any(), any())).thenAnswer(invocation -> {
            String staffId = invocation.getArgument(0);
            if ("s1".equals(staffId)) {
                return List.of(LeaveRequestJpa.builder()
                        .id("a1")
                        .staffId("s1")
                        .status(LeaveStatus.APPROVED)
                        .startDate(yearStart)
                        .endDate(yearStart.plusDays(1))
                        .build());
            }
            return List.of(LeaveRequestJpa.builder()
                    .id("a2")
                    .staffId("s2")
                    .status(LeaveStatus.APPROVED)
                    .startDate(yearStart.plusDays(5))
                    .endDate(yearStart.plusDays(6))
                    .build());
        });

        Map<String, Object> snapshot = controller.teamSnapshot("manager");

        assertThat(snapshot).containsEntry("teamSize", 2);
        assertThat(snapshot).containsEntry("pendingCount", 1);
        assertThat(snapshot).containsKey("approvedDaysUsedByStaff");
        Map<?, ?> usedByStaff = (Map<?, ?>) snapshot.get("approvedDaysUsedByStaff");
        assertThat(usedByStaff).containsEntry("s1", 2);
        assertThat(usedByStaff).containsEntry("s2", 2);
    }
}
