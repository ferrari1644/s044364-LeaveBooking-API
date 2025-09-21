package com.staffs.api.leave.api;

import java.time.LocalDate;

public record LeaveResponse(String id, String staffId, LocalDate startDate, LocalDate endDate, String status) {
}
