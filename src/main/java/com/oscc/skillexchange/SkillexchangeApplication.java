package com.oscc.skillexchange;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SkillexchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillexchangeApplication.class, args);
	}
	@PostConstruct
	public void testEnv() {
		System.out.println("MONGO ENV = " + System.getenv("MONGODB_URI"));
	}
}
