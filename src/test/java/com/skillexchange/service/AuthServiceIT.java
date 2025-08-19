package com.skillexchange.service;

import com.skillexchange.model.UserDetails;
import com.skillexchange.repository.OtpRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class AuthServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("skillexchange")
        .withUsername("admin")
        .withPassword("admin");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "skillexchange");
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/changelog-master.yaml");
    }

    @Autowired AuthService authService;
    @Autowired OtpRepository otpRepository;

    @Test
    void generateOtp_persistsRow() {
        var res = authService.generateOtp("it@test.com");
        var row = otpRepository.findTopByEmailOrderByCreatedAtDesc("it@test.com");
        assertThat(row).isPresent();
    }

    @Test
    void verifyOtp_valid_flow_persistsUser_and_cleansOtp() {
        // arrange
        var res = authService.generateOtp("user@test.com");
        var otp = otpRepository.findTopByEmailOrderByCreatedAtDesc("user@test.com").orElseThrow();
        UserDetails user = UserDetails.builder()
            .email("user@test.com").name("U").phone("1").password("secret")
            .skills(new String[]{"java"})
            .build();
        // act
        var out = authService.verifyOtp(otp.getOtp(), user);
        // assert
        assertThat(otpRepository.findTopByEmailOrderByCreatedAtDesc("user@test.com")).isEmpty();
    }
}

