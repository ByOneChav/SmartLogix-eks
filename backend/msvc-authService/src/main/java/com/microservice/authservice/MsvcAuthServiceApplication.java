package com.microservice.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MsvcAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcAuthServiceApplication.class, args);
	}

}
