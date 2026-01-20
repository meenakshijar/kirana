package com.example.kirana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.kirana.repository.mongo")
public class

KiranaApplication {

	public static void main(String[] args) {
		SpringApplication.run(KiranaApplication.class, args);

	}
}

