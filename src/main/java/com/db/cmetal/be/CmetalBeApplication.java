package com.db.cmetal.be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CmetalBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CmetalBeApplication.class, args);
	}

}
