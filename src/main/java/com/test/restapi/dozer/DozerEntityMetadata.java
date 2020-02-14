package com.test.restapi.dozer;

import org.springframework.data.repository.core.EntityMetadata;

public interface DozerEntityMetadata<T> extends EntityMetadata<T> {
	
	Class<?> getTargetEntityClass();
}
