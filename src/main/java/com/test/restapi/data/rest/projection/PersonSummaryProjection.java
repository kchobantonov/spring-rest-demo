package com.test.restapi.data.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import com.test.restapi.data.rest.PersonResource;

@Projection(name = "summary", types = PersonResource.class)
public interface PersonSummaryProjection {

	String getFirstName();
}
