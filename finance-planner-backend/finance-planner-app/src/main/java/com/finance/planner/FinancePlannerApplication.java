package com.finance.planner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FinancePlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancePlannerApplication.class, args);
    }
}
