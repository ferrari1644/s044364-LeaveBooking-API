package com.staffs.api.leave.infrastructure;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="staff")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StaffJpa {
    @Id private String id;             // using email as id for demo
    @Column(nullable=false) private String fullName;
    private String department;
    private String managerId;
    @Column(nullable=false) private Integer annualLeaveAllocation; // e.g., 25
    @Column(nullable=false) private Integer leaveRemaining; // current balance for business year

    @PrePersist @PreUpdate
    private void ensureLeaveBalances() {
        if (annualLeaveAllocation == null) {
            annualLeaveAllocation = 0;
        }
        if (leaveRemaining == null) {
            leaveRemaining = annualLeaveAllocation;
        }
        if (leaveRemaining < 0) {
            leaveRemaining = 0;
        }
        if (leaveRemaining > annualLeaveAllocation) {
            leaveRemaining = annualLeaveAllocation;
        }
    }
}

