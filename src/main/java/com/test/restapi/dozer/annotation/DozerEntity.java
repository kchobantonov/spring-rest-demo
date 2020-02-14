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
	 * <p>
	 * The adapted (backing) domain class for which a Spring repository of type
	 * {@code org.springframework.data.repository.PagingAndSortingRepository} should exist in the context.
	 * 
	 * <p>
	 * There should be also a Dozer mapping between the {@code DozerEntity} annotated class
	 * and the {@code adaptedDomainClass} as well as mapping for their ID classes
	 * </p>
	 * 
	 * @return the adapted domain class.
	 */
	Class<?> adaptedDomainClass();
}
