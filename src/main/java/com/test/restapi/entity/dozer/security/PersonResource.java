package com.test.restapi.entity.dozer.security;

import org.springframework.data.annotation.Id;
import org.springframework.data.dozer.annotation.DozerEntity;

import com.test.restapi.entity.jpa.security.Person;

import lombok.Data;

@DozerEntity(adaptedDomainClass = Person.class)
@Data
public class PersonResource {

	@Id
	private String id;

	private String firstName;
	private String lastName;

}
