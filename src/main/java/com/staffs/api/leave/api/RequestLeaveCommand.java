package com.staffs.api.leave.api;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

record RequestLeaveCommand(@NotNull LocalDate startDate, @NotNull LocalDate endDate) {
}
