package com.staffs.api.leave.application;

import java.time.*;

public class BusinessYear {
    // Given "today", return the start/end of the current business year (Apr 1 -> Mar 31)
    public static LocalDate start(LocalDate today) {
        LocalDate apr1 = LocalDate.of(today.getYear(), 4, 1);
        return (today.isBefore(apr1)) ? apr1.minusYears(1) : apr1;
    }
    public static LocalDate end(LocalDate today) {
        return start(today).plusYears(1).minusDays(1);
    }
}

