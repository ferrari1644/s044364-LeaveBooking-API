package com.staffs.api.leave.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AmendStaffRequestTest {

    @Test
    void exposesConstructorValues() {
        AmendStaffRequest request = new AmendStaffRequest("IT", 30, 25);

        assertThat(request.department()).isEqualTo("IT");
        assertThat(request.annualLeaveAllocation()).isEqualTo(30);
        assertThat(request.leaveRemaining()).isEqualTo(25);
    }
}
