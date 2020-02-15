package com.test.restapi.entity.dozer.security;

import java.util.UUID;

import com.github.dozermapper.core.BeanFactory;
import com.github.dozermapper.core.MappingException;
import com.github.dozermapper.core.config.BeanContainer;

public class UUIDBeanFactory implements BeanFactory {
	
	@Override
	public Object createBean(Object source, Class<?> sourceClass, String targetBeanId, BeanContainer beanContainer) {
		if (source == null) {
			return null;
		}

		if (String.class.isAssignableFrom(sourceClass)) {
			try {
				return UUID.fromString((String) source);
			} catch (Exception e) {
				throw new MappingException(e);
			}
		}

		throw new MappingException("Unable to convert " + sourceClass + " to UUID");
	}
}
