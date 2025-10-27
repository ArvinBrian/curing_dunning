package com.example.curingdunning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CuringdunningApplication {

	public static void main(String[] args) {
		SpringApplication.run(CuringdunningApplication.class, args);
		System.out.println("SpringBoot Application Started");
	}

}
