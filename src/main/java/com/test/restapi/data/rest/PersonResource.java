package com.test.restapi.data.rest;

import org.springframework.data.annotation.Id;

import com.test.restapi.data.jpa.Person;
import com.test.restapi.dozer.annotation.DozerEntity;

import lombok.Data;

@DozerEntity(domainClass = Person.class)
@Data
public class PersonResource {
	
	@Id
	private String id;
}
