package com.staffs.api.leave.infrastructure;

import com.staffs.api.leave.domain.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequestJpa, String> {
    List<LeaveRequestJpa> findByStaffIdOrderByCreatedAtDesc(String staffId);
    List<LeaveRequestJpa> findByStatus(LeaveStatus status);
    List<LeaveRequestJpa> findByStatusAndStaffIdIn(LeaveStatus status, List<String> staffIds);
    List<LeaveRequestJpa> findByStaffIdIn(List<String> staffIds);

    // For analytics within business year
    List<LeaveRequestJpa> findByStaffIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            String staffId, LeaveStatus status, LocalDate yearStart, LocalDate yearEnd);
}

