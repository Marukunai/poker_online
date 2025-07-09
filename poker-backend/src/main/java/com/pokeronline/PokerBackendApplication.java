package com.pokeronline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PokerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokerBackendApplication.class, args);
	}

}
