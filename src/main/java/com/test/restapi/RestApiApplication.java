package com.test.restapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.dozer.jpa.repository.config.EnableDozerJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.test.restapi.repository.jpa")
@EnableDozerJpaRepositories(basePackages = "com.test.restapi.repository.dozer")
public class RestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestApiApplication.class, args);
	}

	@Configuration
	static class ValidationConfiguration implements RepositoryRestConfigurer {

		@Override
		public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
			validatingListener.addValidator("beforeCreate", validator());
			validatingListener.addValidator("beforeSave", validator());
		}

		@Bean
		@Primary
		public Validator validator() {
			return new LocalValidatorFactoryBean();
		}

	}
}
