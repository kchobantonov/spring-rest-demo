package com.test.restapi.dozer;

import org.springframework.data.repository.core.EntityInformation;

public interface DozerEntityInformation<T, ID> extends EntityInformation<T, ID>, DozerEntityMetadata<T> {

}
