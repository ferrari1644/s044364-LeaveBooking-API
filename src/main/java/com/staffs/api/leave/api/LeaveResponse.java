package com.staffs.api.leave.api;

import java.time.LocalDate;

record LeaveResponse(String id, String staffId, LocalDate startDate, LocalDate endDate, String status) {
}
