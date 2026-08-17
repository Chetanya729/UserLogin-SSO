package com.example.SSO_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SsoProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsoProjectApplication.class, args);
		System.out.println("CLIENT_ID: [" + System.getenv("GOOGLE_CLIENT_ID") + "]");
		System.out.println("CLIENT_SECRET length: " + (System.getenv("GOOGLE_CLIENT_SECRET") == null ? "NULL" : System.getenv("GOOGLE_CLIENT_SECRET").length()));
		System.out.println("CLIENT_ID: [" + System.getenv("GIT_CLIENT_ID") + "]");
		System.out.println("CLIENT_SECRET length: " +
				(System.getenv("GIT_CLIENT_SECRET") == null ? "NULL" : System.getenv("GIT_CLIENT_SECRET").length()));
	}

}
