package com.test.restapi.repository.dozer;

import org.springframework.data.dozer.repository.DozerRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.validation.annotation.Validated;

@NoRepositoryBean
@Validated
public interface ValidatedDozerRepository<T, ID> extends DozerRepository<T, ID> {
	
}
