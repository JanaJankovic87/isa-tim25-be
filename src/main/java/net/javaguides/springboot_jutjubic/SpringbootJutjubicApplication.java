package net.javaguides.springboot_jutjubic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SpringbootJutjubicApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootJutjubicApplication.class, args);
	}

}
