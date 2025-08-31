package com.staffs.api.leave.infrastructure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StaffRepository extends JpaRepository<StaffJpa, String> {
    List<StaffJpa> findByManagerId(String managerId);
    List<StaffJpa> findByDepartment(String department);
}

