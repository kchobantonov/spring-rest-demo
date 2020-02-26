package com.test.restapi.config;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OrderWebMvcConfigurersBeanPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof DelegatingWebMvcConfiguration) {
			Field configurersField = ReflectionUtils.findField(DelegatingWebMvcConfiguration.class, "configurers");
			Assert.notNull(configurersField,
					"Field named 'configurers' not found on " + DelegatingWebMvcConfiguration.class.getName());
			ReflectionUtils.makeAccessible(configurersField);
			Object webMvcConfigurerComposite = ReflectionUtils.getField(configurersField, bean);

			Field delegatesField = ReflectionUtils.findField(webMvcConfigurerComposite.getClass(), "delegates",
					List.class);
			Assert.notNull(delegatesField,
					"Field named 'delegates' not found on " + webMvcConfigurerComposite.getClass().getName());
			ReflectionUtils.makeAccessible(delegatesField);
			List<WebMvcConfigurer> configurers = (List<WebMvcConfigurer>) ReflectionUtils.getField(delegatesField,
					webMvcConfigurerComposite);

			Assert.notNull(configurers, "The value of field 'delegates' is null for object of type "
					+ webMvcConfigurerComposite.getClass().getName());
			configurers.sort(new WebMvcConfigurerComparator());
		}

		return bean;
	}

	private static class WebMvcConfigurerComparator extends AnnotationAwareOrderComparator {
		@Override
		protected int getOrder(Object obj) {
			if (obj != null) {
				Integer order = findOrder(obj);
				if (order != null) {
					return order;
				}
			}
			// if order is not specified then default it before the last one so that if we
			// have
			// explicitly specified @Order with lowest priority then those will be at the
			// end
			return Ordered.LOWEST_PRECEDENCE - 1;
		}
	}
}
