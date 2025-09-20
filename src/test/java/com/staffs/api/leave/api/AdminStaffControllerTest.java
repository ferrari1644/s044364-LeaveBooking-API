package com.staffs.api.leave.api;

import com.staffs.api.leave.infrastructure.StaffJpa;
import com.staffs.api.leave.infrastructure.StaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminStaffControllerTest {

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private AdminStaffController controller;

    @Test
    void addBackfillsLeaveRemainingWhenMissing() {
        var incoming = StaffJpa.builder()
                .id("staff")
                .fullName("Test Staff")
                .annualLeaveAllocation(25)
                .build();
        when(staffRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = controller.add(incoming);

        assertThat(result.getLeaveRemaining()).isEqualTo(25);
        verify(staffRepository).save(incoming);
    }

    @Test
    void updateAmendsDepartmentAndAllocationAndRecalculatesBalance() {
        var existing = StaffJpa.builder()
                .id("staff")
                .fullName("Existing")
                .department("Ops")
                .annualLeaveAllocation(20)
                .leaveRemaining(5)
                .build();
        when(staffRepository.findById("staff")).thenReturn(Optional.of(existing));
        when(staffRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var incoming = StaffJpa.builder()
                .department("IT")
                .annualLeaveAllocation(30)
                .build();

        var updated = controller.update("staff", incoming);

        assertThat(updated.getDepartment()).isEqualTo("IT");
        assertThat(updated.getAnnualLeaveAllocation()).isEqualTo(30);
        assertThat(updated.getLeaveRemaining()).isEqualTo(15);
        verify(staffRepository).save(existing);
    }

    @Test
    void updateClampsExplicitLeaveRemainingToAllocation() {
        var existing = StaffJpa.builder()
                .id("staff")
                .fullName("Existing")
                .annualLeaveAllocation(20)
                .leaveRemaining(10)
                .build();
        when(staffRepository.findById("staff")).thenReturn(Optional.of(existing));
        when(staffRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var incoming = StaffJpa.builder()
                .leaveRemaining(50)
                .build();

        var updated = controller.update("staff", incoming);

        assertThat(updated.getLeaveRemaining()).isEqualTo(20);
        verify(staffRepository).save(existing);
    }
}
