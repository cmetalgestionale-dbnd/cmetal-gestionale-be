package com.db.cmetal.gestionale.be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CmetalBeApplication {

	public static void main(String[] args) {
		System.setProperty("https.protocols", "TLSv1.2");
		SpringApplication.run(CmetalBeApplication.class, args);
	}

}
