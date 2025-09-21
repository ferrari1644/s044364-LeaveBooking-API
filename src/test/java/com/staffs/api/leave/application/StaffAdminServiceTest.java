package com.staffs.api.leave.application;

import com.staffs.api.leave.infrastructure.StaffJpa;
import com.staffs.api.leave.infrastructure.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffAdminServiceTest {

    @Mock
    private StaffRepository staffRepo;
    private StaffAdminService service;

    @BeforeEach
    void setUp() {
        service = new StaffAdminService(staffRepo);
    }

    @Test
    void add_initialisesLeaveBalanceWhenMissing() {
        var staff = StaffJpa.builder()
                .id("new@corp")
                .annualLeaveAllocation(22)
                .leaveRemaining(null)
                .build();
        service.add(staff);
        verify(staffRepo).save(staff);
        assertThat(staff.getLeaveRemaining()).isEqualTo(22);
    }

    @Test
    void amend_updatesDepartmentAndRecalculatesBalanceFromAllocation() {
        var existing = StaffJpa.builder()
                .id("existing@corp")
                .department("Old")
                .annualLeaveAllocation(20)
                .leaveRemaining(15)
                .build();
        when(staffRepo.findById("existing@corp")).thenReturn(Optional.of(existing));

        var updated = StaffJpa.builder().build();
        when(staffRepo.save(existing)).thenReturn(updated);

        var result = service.amend("existing@corp", "Engineering", 25, null);

        assertThat(existing.getDepartment()).isEqualTo("Engineering");
        assertThat(existing.getAnnualLeaveAllocation()).isEqualTo(25);
        assertThat(existing.getLeaveRemaining()).isEqualTo(20);
        assertThat(result).isEqualTo(updated);
    }

    @Test
    void amend_respectsProvidedLeaveRemainingBounds() {
        var existing = StaffJpa.builder()
                .id("bounded@corp")
                .annualLeaveAllocation(18)
                .leaveRemaining(10)
                .build();
        when(staffRepo.findById("bounded@corp")).thenReturn(Optional.of(existing));

        service.amend("bounded@corp", null, null, 50);

        assertThat(existing.getLeaveRemaining()).isEqualTo(18);
        verify(staffRepo).save(existing);
    }
}