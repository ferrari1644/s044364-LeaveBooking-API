package com.staffs.api.leave.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveRequest {
    private String id;
    private String staffId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
    private LocalDate createdAt;

    public static LeaveRequest createPending(String staffId, LocalDate start, LocalDate end) {
        if (end.isBefore(start)) throw new IllegalArgumentException("End before start");
        return LeaveRequest.builder()
                .id(UUID.randomUUID().toString())
                .staffId(staffId)
                .startDate(start)
                .endDate(end)
                .status(LeaveStatus.PENDING)
                .createdAt(LocalDate.now())
                .build();
    }

    public int daysInclusive() { return (int)(endDate.toEpochDay() - startDate.toEpochDay() + 1); }
}

