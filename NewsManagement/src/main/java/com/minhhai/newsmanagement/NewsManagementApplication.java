package com.minhhai.newsmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NewsManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsManagementApplication.class, args);
	}

}
