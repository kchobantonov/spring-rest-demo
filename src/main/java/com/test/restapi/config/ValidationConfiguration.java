package com.test.restapi.config;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
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
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.handler.MappedInterceptor;

import com.test.restapi.validation.HttpMethodValidationGroup;

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

	@Bean
	public MappedInterceptor exposeHttpMethodHandlerInterceptor() {
		return new MappedInterceptor(null, new ExposeHttpMethodHandlerInterceptor());
	}

	// @Override
	/*
	 * public void addInterceptors(InterceptorRegistry registry) {
	 * registry.addInterceptor(new ExposeHttpMethodHandlerInterceptor()); }
	 */

	private class RestValidationGroupsLocalValidatorFactoryBean extends LocalValidatorFactoryBean {
		@Override
		public Validator getValidator() {
			return new RestValidationGroupsValidator(super.getValidator());
		}

		@Override
		public void afterPropertiesSet() {
			super.afterPropertiesSet();

			Field field = ReflectionUtils.findField(LocalValidatorFactoryBean.class, "targetValidator",
					javax.validation.Validator.class);
			Assert.notNull(field,
					"Unabled to find field 'targetValidator' on class " + LocalValidatorFactoryBean.class);
			ReflectionUtils.makeAccessible(field);
			ReflectionUtils.setField(field, this, getValidator());
		}
	}

	private class RestValidationGroupsValidator implements Validator {
		private Validator delegate;

		RestValidationGroupsValidator(Validator delegate) {
			this.delegate = delegate;
		}

		public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
			HttpMethod method = ExposeHttpMethodHandlerInterceptor.CURRENT_METHOD.get();
			if (groups != null) {
				groups = Arrays.copyOf(groups, groups.length + 1);
				groups[groups.length - 1] = toHttpValidationGroup(method);
			} else {
				groups = new Class[] { toHttpValidationGroup(method) };
			}
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
			// TODO hamdle this case as well
			return delegate.forExecutables();
		}

	}

	private Class<? extends HttpMethodValidationGroup> toHttpValidationGroup(HttpMethod method) {
		switch (method) {
		case GET:
			return HttpMethodValidationGroup.GET.class;
		case HEAD:
			return HttpMethodValidationGroup.HEAD.class;
		case POST:
			return HttpMethodValidationGroup.POST.class;
		case PUT:
			return HttpMethodValidationGroup.PUT.class;
		case PATCH:
			return HttpMethodValidationGroup.PATCH.class;
		case DELETE:
			return HttpMethodValidationGroup.DELETE.class;
		case OPTIONS:
			return HttpMethodValidationGroup.OPTIONS.class;
		case TRACE:
			return HttpMethodValidationGroup.TRACE.class;
		default:
			throw new IllegalArgumentException("Unsupported method " + method);
		}
	}

	private static class ExposeHttpMethodHandlerInterceptor extends HandlerInterceptorAdapter {
		static final ThreadLocal<HttpMethod> CURRENT_METHOD = new ThreadLocal<>();

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
				throws Exception {
			CURRENT_METHOD.set(HttpMethod.resolve(request.getMethod()));
			return super.preHandle(request, response, handler);
		}

		@Override
		public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
				ModelAndView modelAndView) throws Exception {
			try {
				super.postHandle(request, response, handler, modelAndView);
			} finally {
				CURRENT_METHOD.set(null);
			}
		}

		@Override
		public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
				Exception ex) throws Exception {
			super.afterCompletion(request, response, handler, ex);
		}
	}
}
