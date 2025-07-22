// src/main/java/com/example/prManagement/PrManagementApplication.java
package com.example.prManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PrManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrManagementApplication.class, args);
	}

}