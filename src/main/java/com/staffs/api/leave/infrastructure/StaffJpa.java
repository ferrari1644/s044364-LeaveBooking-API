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
    @Column(nullable=false) private int annualLeaveAllocation; // e.g., 25
}

