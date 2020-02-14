package com.test.restapi.dozer.repository.support;

import javax.persistence.EntityManager;

import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.util.Assert;

import com.test.restapi.dozer.repository.query.DefaultDozerEntityMetadata;
import com.test.restapi.dozer.repository.query.DozerEntityMetadata;

public abstract class DozerEntityInformationSupport<T, ID> extends AbstractEntityInformation<T, ID>
		implements DozerEntityInformation<T, ID> {
	private DozerEntityMetadata<T> metadata;

	/**
	 * Creates a new {@link DozerEntityInformationSupport} with the given domain
	 * class.
	 *
	 * @param domainClass must not be {@literal null}.
	 */
	public DozerEntityInformationSupport(Class<T> domainClass) {
		super(domainClass);
		this.metadata = new DefaultDozerEntityMetadata<T>(domainClass);
	}

	/**
	 * Creates a {@link DozerEntityInformation} for the given domain class and
	 * {@link EntityManager}.
	 *
	 * @param domainClass must not be {@literal null}.
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> DozerEntityInformation<T, ?> getEntityInformation(Class<T> domainClass) {

		Assert.notNull(domainClass, "Domain class must not be null!");

		if (Persistable.class.isAssignableFrom(domainClass)) {
			return new DozerPersistableEntityInformation(domainClass);
		} else {
			return new DozerMetamodelEntityInformation(domainClass);
		}
	}

	@Override
	public Class<?> getTargetEntityClass() {
		return metadata.getTargetEntityClass();
	}
}
