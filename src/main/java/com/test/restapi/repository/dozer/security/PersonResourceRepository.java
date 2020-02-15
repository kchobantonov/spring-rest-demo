package com.test.restapi.repository.dozer.security;

import org.springframework.data.dozer.repository.DozerRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.test.restapi.entity.dozer.security.PersonResource;

@RepositoryRestResource(collectionResourceRel = "people", itemResourceRel = "person", path = "people")
public interface PersonResourceRepository extends DozerRepository<PersonResource, String> {

}
