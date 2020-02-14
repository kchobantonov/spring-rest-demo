package com.test.restapi.dozer.repository.support;

import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.repository.NoRepositoryBean;

import com.test.restapi.dozer.repository.DozerRepository;

@NoRepositoryBean
public interface DozerRepositoryImplementation<T, ID>
		extends DozerRepository<T, ID>/* , DozerSpecificationExecutor<T> */ {
	/**
	 * Configures the {@link EscapeCharacter} to be used with the repository.
	 *
	 * @param escapeCharacter Must not be {@literal null}.
	 */
	default void setEscapeCharacter(EscapeCharacter escapeCharacter) {

	}
	
	void validateAfterRefresh();
}
