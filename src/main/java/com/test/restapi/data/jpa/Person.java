package com.test.restapi.data.jpa;

import javax.persistence.Basic;
import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Person extends User {

	@Basic
	private String firstName;

	@Basic
	private String lastName;

	@Basic
	private String title;

}
