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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveApplicationServicePendingQueryTest {

    @Mock
    private LeaveRequestRepository leaveRepo;

    @Mock
    private StaffRepository staffRepo;

    @InjectMocks
    private LeaveApplicationService service;

    @Test
    void pendingForTeam_filtersRequestsByDateRange() {
        var team = List.of(
                StaffJpa.builder().id("s1").build(),
                StaffJpa.builder().id("s2").build()
        );
        when(staffRepo.findByManagerId("mgr-1")).thenReturn(team);

        var requests = List.of(
                LeaveRequestJpa.builder().id("r1").staffId("s1").status(LeaveStatus.PENDING)
                        .startDate(LocalDate.of(2024, 4, 10)).endDate(LocalDate.of(2024, 4, 12)).build(),
                LeaveRequestJpa.builder().id("r2").staffId("s2").status(LeaveStatus.PENDING)
                        .startDate(LocalDate.of(2024, 5, 5)).endDate(LocalDate.of(2024, 5, 7)).build(),
                LeaveRequestJpa.builder().id("r3").staffId("s2").status(LeaveStatus.PENDING)
                        .startDate(LocalDate.of(2024, 6, 1)).endDate(LocalDate.of(2024, 6, 3)).build()
        );
        when(leaveRepo.findByStatusAndStaffIdIn(LeaveStatus.PENDING, List.of("s1", "s2")))
                .thenReturn(requests);

        var from = LocalDate.of(2024, 5, 1);
        var to = LocalDate.of(2024, 5, 31);
        var result = service.pendingForTeam("mgr-1", from, to);

        assertThat(result).extracting(LeaveRequestJpa::getId).containsExactly("r2");
    }

    @Test
    void pendingByStaff_returnsOnlyPendingEntries() {
        var requests = List.of(
                LeaveRequestJpa.builder().id("r1").status(LeaveStatus.PENDING).build(),
                LeaveRequestJpa.builder().id("r2").status(LeaveStatus.APPROVED).build(),
                LeaveRequestJpa.builder().id("r3").status(LeaveStatus.PENDING).build()
        );
        when(leaveRepo.findByStaffIdOrderByCreatedAtDesc("staff-1")).thenReturn(requests);

        var result = service.pendingByStaff("staff-1");

        assertThat(result).extracting(LeaveRequestJpa::getId).containsExactly("r1", "r3");
    }
}
