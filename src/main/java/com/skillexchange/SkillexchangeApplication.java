package com.skillexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;

@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class)
public class SkillexchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillexchangeApplication.class, args);
    }
}
