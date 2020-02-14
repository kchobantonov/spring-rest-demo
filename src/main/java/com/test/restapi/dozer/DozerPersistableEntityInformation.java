package com.test.restapi.dozer;

import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

public class DozerPersistableEntityInformation<T extends Persistable<ID>, ID>
		extends DozerMetamodelEntityInformation<T, ID> {

	/**
	 * Creates a new {@link DozerPersistableEntityInformation} for the given domain
	 * class.
	 *
	 * @param domainClass must not be {@literal null}.
	 */
	public DozerPersistableEntityInformation(Class<T> domainClass) {
		super(domainClass);
	}

	@Override
	public boolean isNew(T entity) {
		return entity.isNew();
	}

	@Nullable
	@Override
	public ID getId(T entity) {
		return entity.getId();
	}

}
