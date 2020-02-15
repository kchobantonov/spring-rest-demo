package com.test.restapi.entity.dozer.security.projection;

import org.springframework.data.rest.core.config.Projection;

import com.test.restapi.entity.dozer.security.PersonResource;

@Projection(name = "summary", types = PersonResource.class)
public interface PersonSummaryProjection {

	String getFirstName();
}
