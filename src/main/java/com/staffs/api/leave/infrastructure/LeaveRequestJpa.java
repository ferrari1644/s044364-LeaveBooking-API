package com.staffs.api.leave.infrastructure;

import com.staffs.api.leave.domain.model.LeaveStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="leave_request")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveRequestJpa {
    @Id private String id;
    @Column(nullable=false) private String staffId;
    @Column(nullable=false) private LocalDate startDate;
    @Column(nullable=false) private LocalDate endDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private LeaveStatus status;
    @Column(nullable=false) private LocalDate createdAt;
}

