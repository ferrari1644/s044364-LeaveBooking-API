package com.staffs.api.leave.api;

import com.staffs.api.leave.application.LeaveApplicationService;
import com.staffs.api.leave.infrastructure.LeaveRequestJpa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerControllerTest {

    @Mock
    private LeaveApplicationService leaveApplicationService;

    @InjectMocks
    private ManagerController controller;

    @Test
    void pendingReturnsTeamRequestsWithinDates() {
        LocalDate from = LocalDate.of(2024, 5, 1);
        LocalDate to = LocalDate.of(2024, 5, 31);
        var expected = List.of(LeaveRequestJpa.builder().id("1").build());
        when(leaveApplicationService.pendingForTeam("manager@example.com", from, to)).thenReturn(expected);

        var result = controller.pending("manager@example.com", from, to);

        assertThat(result).isSameAs(expected);
        verify(leaveApplicationService).pendingForTeam("manager@example.com", from, to);
    }

    @Test
    void approveDelegatesToService() {
        controller.approve("leave-1");

        verify(leaveApplicationService).approve("leave-1");
    }

    @Test
    void rejectDelegatesToService() {
        controller.reject("leave-2");

        verify(leaveApplicationService).reject("leave-2");
    }

    @Test
    void staffBalanceReturnsRemainingDaysForStaff() {
        when(leaveApplicationService.remainingForStaff(eq("staff-1"), any(LocalDate.class))).thenReturn(9);

        int balance = controller.staffBalance("staff-1");

        assertThat(balance).isEqualTo(9);
        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(leaveApplicationService).remainingForStaff(eq("staff-1"), captor.capture());
        assertThat(captor.getValue()).isEqualTo(LocalDate.now());
    }
}
