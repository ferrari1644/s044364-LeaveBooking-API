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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveApplicationServiceRequestTest {

    @Mock
    private LeaveRequestRepository leaveRepo;

    @Mock
    private StaffRepository staffRepo;

    @InjectMocks
    private LeaveApplicationService service;

    @Test
    void requestLeave_persistsPendingRequestWhenStaffExists() {
        var staff = StaffJpa.builder().id("staff-1").build();
        when(staffRepo.findById("staff-1")).thenReturn(Optional.of(staff));
        var start = LocalDate.of(2024, 1, 10);
        var end = start.plusDays(2);

        var result = service.requestLeave("staff-1", start, end);

        ArgumentCaptor<LeaveRequestJpa> captor = ArgumentCaptor.forClass(LeaveRequestJpa.class);
        verify(leaveRepo).save(captor.capture());
        LeaveRequestJpa saved = captor.getValue();
        assertEquals("staff-1", saved.getStaffId());
        assertEquals(start, saved.getStartDate());
        assertEquals(end, saved.getEndDate());
        assertEquals(LeaveStatus.PENDING, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
        assertEquals(saved.getId(), result.getId());
        assertEquals(LeaveStatus.PENDING, result.getStatus());
    }
}
