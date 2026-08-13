package com.firstclub.membership;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * {@code @SpringBootApplication} enables component scanning and Spring Boot's
 * automatic configuration. All application code will live in this package or
 * one of its subpackages so Spring can discover it.
 */

@SpringBootApplication
public class MembershipServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MembershipServiceApplication.class, args);
    }
}
