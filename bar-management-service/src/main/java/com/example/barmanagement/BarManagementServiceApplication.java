package com.example.barmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class BarManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarManagementServiceApplication.class, args);
	}

}
