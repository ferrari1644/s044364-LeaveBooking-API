package com.staffs.api.leave.api;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RequestLeaveCommand(@NotNull LocalDate startDate, @NotNull LocalDate endDate) {
}
