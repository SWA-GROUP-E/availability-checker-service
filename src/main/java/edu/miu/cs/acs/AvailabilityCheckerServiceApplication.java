package edu.miu.cs.acs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class AvailabilityCheckerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AvailabilityCheckerServiceApplication.class, args);
	}

}
