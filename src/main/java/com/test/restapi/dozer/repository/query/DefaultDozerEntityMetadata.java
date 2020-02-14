package com.test.restapi.dozer.repository.query;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;

import com.test.restapi.dozer.annotation.DozerEntity;

public class DefaultDozerEntityMetadata<T> implements DozerEntityMetadata<T> {
	private final Class<T> domainType;

	/**
	 * Creates a new {@link DefaultDozerEntityMetadata} for the given domain type.
	 *
	 * @param domainType must not be {@literal null}.
	 */
	public DefaultDozerEntityMetadata(Class<T> domainType) {

		Assert.notNull(domainType, "Domain type must not be null!");
		this.domainType = domainType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.EntityMetadata#getJavaType()
	 */
	@Override
	public Class<T> getJavaType() {
		return domainType;
	}


	@Override
	public Class<?> getTargetEntityClass() {
		DozerEntity entity = AnnotatedElementUtils.findMergedAnnotation(domainType, DozerEntity.class);
		return entity.domainClass();
	}
}
