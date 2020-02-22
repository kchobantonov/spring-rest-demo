package com.test.restapi.docs;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.hateoas.EntityModel;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.test.restapi.entity.dozer.employee.EmployeeResource;
import com.test.restapi.entity.jpa.employee.Gender;

@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class EmployeeResourceTest extends AbstractResourceRepositoryTest<EmployeeResource, Integer> {

	public EmployeeResourceTest() {
		super(EmployeeResource.class, createExample());
	}

	@Override
	protected TypeReference<EntityModel<EmployeeResource>> getTypeReference() {
		return new TypeReference<EntityModel<EmployeeResource>>() {
		};
	}

	@Override
	protected void applyUpdateChanges(EmployeeResource resource) {
		resource.setFirstName("John");
		resource.setGender(Gender.Male);
	}

	private static EmployeeResource createExample() {
		EmployeeResource resource = new EmployeeResource();
		resource.setFirstName("John");
		resource.setLastName("Doe");
		resource.setGender(Gender.Male);

		return resource;
	}

}
