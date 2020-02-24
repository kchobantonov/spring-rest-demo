package com.test.restapi.config;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.validation.Validator;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.dozer.repository.support.DozerEntityInformation;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.repository.support.DefaultRepositoryInvokerFactory;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.util.Pair;
import org.springframework.util.ReflectionUtils;

import com.github.dozermapper.core.CustomFieldMapper;
import com.github.dozermapper.core.classmap.ClassMap;
import com.github.dozermapper.core.fieldmap.FieldMap;
import com.github.dozermapper.spring.DozerBeanMapperFactoryBean;
import com.github.dozermapper.springboot.autoconfigure.DozerProperties;

@Configuration
@EnableConfigurationProperties({ DozerProperties.class, RepositoryRestProperties.class })
public class DozerRepositoryRestMvcConfiguration extends RepositoryRestMvcConfiguration {
	private final RepositoryRestProperties properties;
	private final Validator validator;
	
	public DozerRepositoryRestMvcConfiguration(ApplicationContext context,
			ObjectFactory<ConversionService> conversionService, RepositoryRestProperties properties, Validator validator) {
		super(context, conversionService);
		this.properties = properties;
		this.validator = validator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		properties.applyTo(repositoryRestConfiguration());
	}

	@Bean
	@Override
	public RepositoryInvokerFactory repositoryInvokerFactory(@Qualifier ConversionService defaultConversionService) {

		return new DozerProjectionOptimizationRepositoryInvokerFactory(repositories(),
				new DefaultRepositoryInvokerFactory(repositories(), defaultConversionService), getEntityLookups(),
				repositoryRestConfiguration().getProjectionConfiguration(), validator);

	}

	@Bean
	public DozerBeanMapperFactoryBean dozerMapper(DozerProperties properties) throws IOException {
		DozerBeanMapperFactoryBean factoryBean = new DozerBeanMapperFactoryBean();
		factoryBean.setMappingFiles(properties.getMappingFiles());
		factoryBean.setCustomFieldMapper(new ProjectionCustomFieldMapper());
		return factoryBean;
	}

	static class ProjectionCustomFieldMapper implements CustomFieldMapper {
		public static final ThreadLocal<Pair<DozerEntityInformation, Class<?>>> CURRENT_DOZER_ENTITY_PROJECTION_CLASS = new ThreadLocal<Pair<DozerEntityInformation, Class<?>>>();

		@Override
		public boolean mapField(Object source, Object destination, Object sourceFieldValue, ClassMap classMap,
				FieldMap fieldMapping) {
			Pair<DozerEntityInformation, Class<?>> currentEntityProjection = CURRENT_DOZER_ENTITY_PROJECTION_CLASS
					.get();
			if (currentEntityProjection != null
					&& currentEntityProjection.getFirst().getJavaType().isInstance(destination)) {
				Class<?> projection = currentEntityProjection.getSecond();

				String fieldName = fieldMapping.getDestFieldName();
				String fieldMethodName = null;

				if (fieldName != null) {
					PersistentProperty property = currentEntityProjection.getFirst().getPersistentEntity()
							.getPersistentProperty(fieldName);
					if (property != null && !property.isIdProperty()) {
						// the id property is required by the resource assembler so we must map it even
						// if this is not in the projection
						Method getter = property.getGetter();
						if (getter != null) {
							fieldMethodName = getter.getName();
						}
					}

				}

				if (fieldMethodName != null && ReflectionUtils.findMethod(projection, fieldMethodName) == null) {
					// mark the field as already mapped since this is not in the projection there is
					// not need to convert it
					return true;
				}

			}

			return false;
		}

	}
}
