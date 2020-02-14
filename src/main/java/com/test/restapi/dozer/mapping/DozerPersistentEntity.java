package com.test.restapi.dozer.mapping;

import org.springframework.data.mapping.PersistentEntity;

public interface DozerPersistentEntity<T> extends PersistentEntity<T, DozerPersistentProperty>  {

}
