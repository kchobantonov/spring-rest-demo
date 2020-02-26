package com.test.restapi.config;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Configuration
public class ValidationConfiguration implements RepositoryRestConfigurer, WebMvcConfigurer {

	@Override
	public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
		// validatingListener.addValidator("beforeCreate", validator());
		// validatingListener.addValidator("beforeSave", validator());
	}

	@Bean
	@Primary
	public Validator validator() {
		return new RestValidationGroupsLocalValidatorFactoryBean();
	}

	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor() {
		MethodValidationPostProcessor result = new MethodValidationPostProcessor();
		result.setValidator(validator());
		return result;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new ExposeHttpMethodHandlerInterceptor());
	}

	private class RestValidationGroupsLocalValidatorFactoryBean extends LocalValidatorFactoryBean {
		@Override
		public Validator getValidator() {
			return new RestValidationGroupsValidator(super.getValidator());
		}
	}

	private class RestValidationGroupsValidator implements Validator {
		private Validator delegate;

		RestValidationGroupsValidator(Validator delegate) {
			this.delegate = delegate;
		}

		public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
			return delegate.validate(object, groups);
		}

		public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
			return delegate.validateProperty(object, propertyName, groups);
		}

		public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value,
				Class<?>... groups) {
			return delegate.validateValue(beanType, propertyName, value, groups);
		}

		public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
			return delegate.getConstraintsForClass(clazz);
		}

		public <T> T unwrap(Class<T> type) {
			return delegate.unwrap(type);
		}

		public ExecutableValidator forExecutables() {
			return delegate.forExecutables();
		}

	}

	private class ExposeHttpMethodHandlerInterceptor extends HandlerInterceptorAdapter {
		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
				throws Exception {

			return super.preHandle(request, response, handler);
		}

		@Override
		public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
				ModelAndView modelAndView) throws Exception {
			super.postHandle(request, response, handler, modelAndView);
		}

		@Override
		public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
				Exception ex) throws Exception {
			super.afterCompletion(request, response, handler, ex);
		}
	}
}
