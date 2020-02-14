package com.test.restapi.dozer;

import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.repository.NoRepositoryBean;

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
