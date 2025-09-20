package com.staffs.api.leave.application;

import com.staffs.api.leave.infrastructure.StaffJpa;
import com.staffs.api.leave.infrastructure.StaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffAdminServiceTest {

    @Mock
    private StaffRepository staffRepo;
    @InjectMocks
    private StaffAdminService service;

    @Test
    void add_setsRemainingToAllocationWhenMissing() {
        var staff = StaffJpa.builder()
                .id("staff-1")
                .fullName("Test User")
                .annualLeaveAllocation(22)
                .leaveRemaining(null)
                .build();

        when(staffRepo.save(any(StaffJpa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.add(staff);

        ArgumentCaptor<StaffJpa> captor = ArgumentCaptor.forClass(StaffJpa.class);
        verify(staffRepo).save(captor.capture());
        assertThat(captor.getValue().getLeaveRemaining()).isEqualTo(22);
        assertThat(saved.getLeaveRemaining()).isEqualTo(22);
    }

    @Test
    void amend_updatesAllocationAndRecomputesRemainingWhenNotProvided() {
        var existing = StaffJpa.builder()
                .id("staff-1")
                .fullName("Test User")
                .department("Sales")
                .annualLeaveAllocation(20)
                .leaveRemaining(5)
                .build();
        when(staffRepo.findById("staff-1")).thenReturn(Optional.of(existing));
        when(staffRepo.save(any(StaffJpa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var amended = service.amend("staff-1", "Marketing", 25, null);

        ArgumentCaptor<StaffJpa> captor = ArgumentCaptor.forClass(StaffJpa.class);
        verify(staffRepo).save(captor.capture());
        StaffJpa saved = captor.getValue();
        assertThat(saved.getDepartment()).isEqualTo("Marketing");
        assertThat(saved.getAnnualLeaveAllocation()).isEqualTo(25);
        assertThat(saved.getLeaveRemaining()).isEqualTo(10);
        assertThat(amended.getLeaveRemaining()).isEqualTo(10);
    }
}
