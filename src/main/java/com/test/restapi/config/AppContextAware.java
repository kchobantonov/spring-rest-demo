package com.test.restapi.config;

import org.springframework.context.ApplicationContext;

public final class AppContextAware {
	public static AppContextAware INSTANCE = new AppContextAware();

	private ApplicationContext applicationContext;

	private AppContextAware() {
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
