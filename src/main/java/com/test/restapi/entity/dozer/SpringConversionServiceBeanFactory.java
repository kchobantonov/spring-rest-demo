package com.test.restapi.entity.dozer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.github.dozermapper.core.BeanFactory;
import com.github.dozermapper.core.MappingException;
import com.github.dozermapper.core.config.BeanContainer;
import com.github.dozermapper.core.util.MappingUtils;

@Component
public class SpringConversionServiceBeanFactory implements BeanFactory {
	
	@Autowired
	private ObjectProvider<ConversionService> defaultConversionService;

	@Override
	public Object createBean(Object source, Class<?> sourceClass, String targetBeanId, BeanContainer beanContainer) {
		Class<?> targetClass = MappingUtils.loadClass(targetBeanId, beanContainer);

		if (defaultConversionService.getObject().canConvert(sourceClass, targetClass)) {
			return defaultConversionService.getObject().convert(source, targetClass);
		}
		throw new MappingException("Unable to convert " + sourceClass + " to UUID");
	}
}
