package com.stockmeds.centurion_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author vineshkumar
 * @created 10/09/24
 */

@EnableCaching
@SpringBootApplication
public class CenturionCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(CenturionCoreApplication.class, args);
	}

}
