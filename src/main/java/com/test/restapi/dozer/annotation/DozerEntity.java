package com.test.restapi.dozer.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.data.annotation.Persistent;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Persistent
public @interface DozerEntity {
	/**
	 * The backing domain class for which a Spring repository of type
	 * PagingAndSortingRepository should exist in the context.
	 * 
	 * @return
	 */
	Class<?> domainClass();
}
