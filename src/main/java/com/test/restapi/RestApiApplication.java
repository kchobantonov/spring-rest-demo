package com.test.restapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.test.restapi.dozer.EnableDozerRepositories;
import com.test.restapi.repository.jpa.PersonRepository;
import com.test.restapi.repository.rest.PersonResourceRepository;

@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = PersonRepository.class)
@EnableDozerRepositories(basePackageClasses = PersonResourceRepository.class)
public class RestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestApiApplication.class, args);
	}

}
