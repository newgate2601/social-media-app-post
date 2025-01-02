package com.example.social_media_app_post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class SocialMediaAppPostApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialMediaAppPostApplication.class, args);
	}

}
