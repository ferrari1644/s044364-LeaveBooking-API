package com.staffs.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // component scan starts at com.staffs.api
public class LeaveBookingApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeaveBookingApiApplication.class, args);
    }
}
