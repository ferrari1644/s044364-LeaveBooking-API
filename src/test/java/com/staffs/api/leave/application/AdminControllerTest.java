package com.staffs.api.leave.api;

import com.staffs.api.leave.application.LeaveApplicationService;
import com.staffs.api.leave.application.StaffAdminService;
import com.staffs.api.leave.infrastructure.LeaveRequestJpa;
import com.staffs.api.leave.infrastructure.StaffJpa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private StaffAdminService staffAdminService;

    @Mock
    private LeaveApplicationService leaveApplicationService;

    @InjectMocks
    private AdminController controller;

    @Test
    void addDelegatesToStaffAdminService() {
        var staff = StaffJpa.builder().id("id").fullName("Test Staff").build();
        when(staffAdminService.add(staff)).thenReturn(staff);

        var result = controller.add(staff);

        assertThat(result).isSameAs(staff);
        verify(staffAdminService).add(staff);
    }

    @Test
    void amendPassesValuesFromRequest() {
        AmendStaffRequest request = new AmendStaffRequest("IT", 30, 25);
        var updated = StaffJpa.builder().id("staff").fullName("Name").build();
        when(staffAdminService.amend("staff", "IT", 30, 25)).thenReturn(updated);

        var response = controller.amend("staff", request);

        assertThat(response).isSameAs(updated);
        verify(staffAdminService).amend("staff", "IT", 30, 25);
    }

    @Test
    void pendingByStaffReturnsPendingRequestsForStaff() {
        var requests = List.of(LeaveRequestJpa.builder().id("1").build());
        when(leaveApplicationService.pendingByStaff("staff")).thenReturn(requests);

        assertThat(controller.pendingByStaff("staff")).isSameAs(requests);
        verify(leaveApplicationService).pendingByStaff("staff");
    }

    @Test
    void pendingByTeamReturnsRequests() {
        var requests = List.of(LeaveRequestJpa.builder().id("2").build());
        when(leaveApplicationService.pendingByManagerTeam("manager")).thenReturn(requests);

        assertThat(controller.pendingByTeam("manager")).isSameAs(requests);
        verify(leaveApplicationService).pendingByManagerTeam("manager");
    }

    @Test
    void pendingCompanyReturnsCompanyWideRequests() {
        var requests = List.of(LeaveRequestJpa.builder().id("3").build());
        when(leaveApplicationService.pendingCompanyWide()).thenReturn(requests);

        assertThat(controller.pendingCompany()).isSameAs(requests);
        verify(leaveApplicationService).pendingCompanyWide();
    }

    @Test
    void approveOnBehalfDelegatesToService() {
        controller.approveOnBehalf("leaveId");

        verify(leaveApplicationService).adminApproveOnBehalf("leaveId");
    }
}
