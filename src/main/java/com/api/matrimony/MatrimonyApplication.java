package com.api.matrimony;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
//@EnableJpaRepositories(basePackages = "com.api.matrimony.repository")
//@EntityScan(basePackages = "com.api.matrimony.entity")
public class MatrimonyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatrimonyApplication.class, args);
	}

}
