package com.store.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
		excludeName = {
				"org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration",
				"org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
		}
)
public class TradeLedger {

	public static void main(String[] args) {
		SpringApplication.run(TradeLedger.class, args);
	}

}
