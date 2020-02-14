package com.test.restapi.dozer.repository.support;

import org.springframework.data.repository.core.EntityInformation;

import com.test.restapi.dozer.repository.query.DozerEntityMetadata;

public interface DozerEntityInformation<T, ID> extends EntityInformation<T, ID>, DozerEntityMetadata<T> {

}
