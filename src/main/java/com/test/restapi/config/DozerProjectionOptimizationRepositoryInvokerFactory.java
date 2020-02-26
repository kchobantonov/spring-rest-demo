package com.test.restapi.config;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.dozer.repository.support.DozerEntityInformation;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.core.projection.ProjectionDefinitions;
import org.springframework.data.rest.core.support.EntityLookup;
import org.springframework.data.util.Pair;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.test.restapi.config.DozerRepositoryRestMvcConfiguration.ProjectionCustomFieldMapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class DozerProjectionOptimizationRepositoryInvokerFactory implements RepositoryInvokerFactory {
	private final Repositories repositories;
	private final RepositoryInvokerFactory delegate;
	private final PluginRegistry<EntityLookup<?>, Class<?>> lookups;
	private final ProjectionDefinitions projectionDefinitions;
	private final Validator validator;

	public DozerProjectionOptimizationRepositoryInvokerFactory(Repositories repositories,
			RepositoryInvokerFactory delegate, List<? extends EntityLookup<?>> lookups,
			ProjectionDefinitions projectionDefinitions, Validator validator) {

		Assert.notNull(delegate, "Delegate RepositoryInvokerFactory must not be null!");
		Assert.notNull(lookups, "EntityLookups must not be null!");
		Assert.notNull(repositories, "Repositories must not be null!");
		Assert.notNull(projectionDefinitions, "ProjectionDefinitions must not be null!");

		this.repositories = repositories;
		this.delegate = delegate;
		this.lookups = PluginRegistry.of(lookups);
		this.projectionDefinitions = projectionDefinitions;
		this.validator = validator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.support.RepositoryInvokerFactory#
	 * getInvokerFor(java.lang.Class)
	 */
	@Override
	public RepositoryInvoker getInvokerFor(Class<?> domainType) {

		Optional<EntityLookup<?>> lookup = lookups.getPluginFor(domainType);

		return new DozerProjectionOptimizationRepositoryInvoker(repositories.getEntityInformationFor(domainType),
				delegate.getInvokerFor(domainType), lookup, projectionDefinitions, validator);
	}

	@RequiredArgsConstructor
	private static class DozerProjectionOptimizationRepositoryInvoker implements RepositoryInvoker {

		private final @NonNull EntityInformation entityInformation;
		private final @NonNull RepositoryInvoker delegate;
		private final @NonNull Optional<EntityLookup<?>> lookup;
		private final @NonNull ProjectionDefinitions projectionDefinitions;
		private final @NonNull Validator validator;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.springframework.data.repository.support.RepositoryInvoker#invokeFindOne(
		 * java.io.Serializable)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public <T> Optional<T> invokeFindById(Object id) {
			Class<?> projectionType = null;
			if (entityInformation instanceof DozerEntityInformation) {
				projectionType = getCurrentProjectionType();
			}
			try {
				ProjectionCustomFieldMapper.CURRENT_DOZER_ENTITY_PROJECTION_CLASS.set(
						projectionType != null ? Pair.of((DozerEntityInformation) entityInformation, projectionType)
								: null);

				return lookup.isPresent() //
						? (Optional<T>) lookup.flatMap(it -> it.lookupEntity(id)) //
						: delegate.invokeFindById(id);
			} finally {
				ProjectionCustomFieldMapper.CURRENT_DOZER_ENTITY_PROJECTION_CLASS.set(null);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.data.repository.support.RepositoryInvoker#
		 * invokeQueryMethod(java.lang.reflect.Method,
		 * org.springframework.util.MultiValueMap,
		 * org.springframework.data.domain.Pageable,
		 * org.springframework.data.domain.Sort)
		 */
		@Override
		public Optional<Object> invokeQueryMethod(Method method, MultiValueMap<String, ? extends Object> parameters,
				Pageable pageable, Sort sort) {
			Class<?> projectionType = null;
			if (entityInformation instanceof DozerEntityInformation) {
				projectionType = getCurrentProjectionType();
			}
			try {
				ProjectionCustomFieldMapper.CURRENT_DOZER_ENTITY_PROJECTION_CLASS.set(
						projectionType != null ? Pair.of((DozerEntityInformation) entityInformation, projectionType)
								: null);
				return delegate.invokeQueryMethod(method, parameters, pageable, sort);
			} finally {
				ProjectionCustomFieldMapper.CURRENT_DOZER_ENTITY_PROJECTION_CLASS.set(null);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.springframework.data.repository.support.RepositoryInvocationInformation#
		 * hasDeleteMethod()
		 */
		@Override
		public boolean hasDeleteMethod() {
			return delegate.hasDeleteMethod();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.springframework.data.repository.support.RepositoryInvocationInformation#
		 * hasFindAllMethod()
		 */
		@Override
		public boolean hasFindAllMethod() {
			return delegate.hasFindAllMethod();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.springframework.data.repository.support.RepositoryInvocationInformation#
		 * hasFindOneMethod()
		 */
		@Override
		public boolean hasFindOneMethod() {
			return delegate.hasFindOneMethod();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.springframework.data.repository.support.RepositoryInvocationInformation#
		 * hasSaveMethod()
		 */
		@Override
		public boolean hasSaveMethod() {
			return delegate.hasSaveMethod();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.data.repository.support.RepositoryInvoker#
		 * invokeDeleteById(java.lang.Object)
		 */
		@Override
		public void invokeDeleteById(Object id) {
			delegate.invokeDeleteById(id);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.springframework.data.repository.support.RepositoryInvoker#invokeFindAll(
		 * org.springframework.data.domain.Pageable)
		 */
		@Override
		public Iterable<Object> invokeFindAll(Pageable pageable) {
			Class<?> projectionType = null;
			if (entityInformation instanceof DozerEntityInformation) {
				projectionType = getCurrentProjectionType();
			}
			try {
				ProjectionCustomFieldMapper.CURRENT_DOZER_ENTITY_PROJECTION_CLASS.set(
						projectionType != null ? Pair.of((DozerEntityInformation) entityInformation, projectionType)
								: null);

				return delegate.invokeFindAll(pageable);
			} finally {
				ProjectionCustomFieldMapper.CURRENT_DOZER_ENTITY_PROJECTION_CLASS.set(null);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.springframework.data.repository.support.RepositoryInvoker#invokeFindAll(
		 * org.springframework.data.domain.Sort)
		 */
		@Override
		public Iterable<Object> invokeFindAll(Sort sort) {
			Class<?> projectionType = null;
			if (entityInformation instanceof DozerEntityInformation) {
				projectionType = getCurrentProjectionType();
			}
			try {
				ProjectionCustomFieldMapper.CURRENT_DOZER_ENTITY_PROJECTION_CLASS.set(
						projectionType != null ? Pair.of((DozerEntityInformation) entityInformation, projectionType)
								: null);
				return delegate.invokeFindAll(sort);
			} finally {
				ProjectionCustomFieldMapper.CURRENT_DOZER_ENTITY_PROJECTION_CLASS.set(null);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.springframework.data.repository.support.RepositoryInvoker#invokeSave(java
		 * .lang.Object)
		 */
		@Override
		public <T> T invokeSave(T object) {
			Set<ConstraintViolation<Object>> result = validator.validate(object);

			if (!result.isEmpty()) {
				throw new ConstraintViolationException(result);
			}

			return delegate.invokeSave(object);
		}

		protected Class<?> getCurrentProjectionType() {
			RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
			if (attributes instanceof ServletRequestAttributes) {
				HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();

				String projectionParameter = (String) request.getParameter(projectionDefinitions.getParameterName());
				if (projectionParameter != null) {
					return projectionDefinitions.getProjectionType(entityInformation.getJavaType(),
							projectionParameter);
				}
			}

			return null;
		}
	}
}
