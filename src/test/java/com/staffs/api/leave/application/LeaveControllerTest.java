package com.staffs.api.leave.application;

import com.staffs.api.leave.api.LeaveController;
import com.staffs.api.leave.api.LeaveResponse;
import com.staffs.api.leave.api.RequestLeaveCommand;
import com.staffs.api.leave.application.LeaveApplicationService;
import com.staffs.api.leave.domain.model.LeaveRequest;
import com.staffs.api.leave.domain.model.LeaveStatus;
import com.staffs.api.leave.infrastructure.LeaveRequestJpa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveControllerTest {

    @Mock
    private LeaveApplicationService leaveApplicationService;

    @InjectMocks
    private LeaveController controller;

    @Test
    void requestConvertsDomainToResponse() {
        LocalDate start = LocalDate.of(2024, 1, 10);
        LocalDate end = LocalDate.of(2024, 1, 12);
        var command = new RequestLeaveCommand(start, end);
        var leave = LeaveRequest.builder()
                .id("leave-id")
                .staffId("user@example.com")
                .startDate(start)
                .endDate(end)
                .status(LeaveStatus.PENDING)
                .build();
        when(leaveApplicationService.requestLeave("user@example.com", start, end)).thenReturn(leave);

        LeaveResponse response = controller.request("user@example.com", command);

        assertThat(response.id()).isEqualTo("leave-id");
        assertThat(response.staffId()).isEqualTo("user@example.com");
        assertThat(response.startDate()).isEqualTo(start);
        assertThat(response.endDate()).isEqualTo(end);
        assertThat(response.status()).isEqualTo("PENDING");
        verify(leaveApplicationService).requestLeave("user@example.com", start, end);
    }

    @Test
    void cancelDelegatesToService() {
        controller.cancel("leave-123");

        verify(leaveApplicationService).cancel("leave-123");
    }

    @Test
    void myReturnsRequestsFromService() {
        var requests = List.of(LeaveRequestJpa.builder().id("1").build());
        when(leaveApplicationService.myRequests("user@example.com")).thenReturn(requests);

        assertThat(controller.my("user@example.com")).isSameAs(requests);
        verify(leaveApplicationService).myRequests("user@example.com");
    }

    @Test
    void myBalanceReturnsRemainingDays() {
        when(leaveApplicationService.remainingDays(eq("user@example.com"), any(LocalDate.class))).thenReturn(7);

        int result = controller.myBalance("user@example.com");

        assertThat(result).isEqualTo(7);
        verify(leaveApplicationService).remainingDays(eq("user@example.com"), any(LocalDate.class));
    }
}
