package com.example.turtledreambackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TurtleDreamBackendApplication {
	
	public static void main(String[] args) {
		System.setProperty("spring.main.allow-circular-references", "true");
		SpringApplication.run(TurtleDreamBackendApplication.class, args);
	}
	
}

