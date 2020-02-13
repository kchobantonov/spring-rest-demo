package com.test.restapi.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
public class JpaConfiguration extends JpaBaseConfiguration {

	protected JpaConfiguration(DataSource dataSource, JpaProperties properties,
			ObjectProvider<JtaTransactionManager> jtaTransactionManager) {
		super(dataSource, properties, jtaTransactionManager);
	}

	@Override
	protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
		return new EclipseLinkJpaVendorAdapter();
	}

	@Override
	protected Map<String, Object> getVendorProperties() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(PersistenceUnitProperties.WEAVING, detectWeavingMode());
		map.put(PersistenceUnitProperties.DDL_GENERATION, "drop-and-create-tables");
		return map;
	}

	private String detectWeavingMode() {
		if (InstrumentationLoadTimeWeaver.isInstrumentationAvailable()) {
			return "true";
		}

		return "static";
	}

	@Bean
	public ApplicationListener<ContextRefreshedEvent> applicationListener() {
		return new ApplicationListener<ContextRefreshedEvent>() {
			public void onApplicationEvent(ContextRefreshedEvent event) {
				AppContextAware.INSTANCE.setApplicationContext(event.getApplicationContext());
			}
		};
	}

}
