package com.staffs.api.leave.application;

import com.staffs.api.leave.api.LeaveResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LeaveResponseTest {

    @Test
    void storesProvidedValues() {
        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate end = LocalDate.of(2024, 2, 5);

        LeaveResponse response = new LeaveResponse("id", "staff", start, end, "PENDING");

        assertThat(response.id()).isEqualTo("id");
        assertThat(response.staffId()).isEqualTo("staff");
        assertThat(response.startDate()).isEqualTo(start);
        assertThat(response.endDate()).isEqualTo(end);
        assertThat(response.status()).isEqualTo("PENDING");
    }
}
