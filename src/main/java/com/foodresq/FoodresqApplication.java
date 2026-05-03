package com.foodresq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FoodresqApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodresqApplication.class, args);

	}

}
