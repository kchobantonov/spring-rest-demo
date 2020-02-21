package com.test.restapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

@Configuration
public class ExposeIdRepositoryRestConfigurer implements RepositoryRestConfigurer {
	@Autowired
	private Repositories repositories;

	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		repositories.iterator().forEachRemaining(it -> config.exposeIdsFor(it));
	}
}
