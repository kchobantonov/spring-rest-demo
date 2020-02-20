package com.test.restapi.repository.dozer.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.dozer.repository.DozerRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.test.restapi.entity.dozer.employee.EmployeeResource;

@RepositoryRestResource(collectionResourceRel = "employees", itemResourceRel = "employee", path = "restemployee")
public interface EmployeeResourceRepository extends DozerRepository<EmployeeResource, Integer> {

	@Query("SELECT e FROM Employee e WHERE e.firstName LIKE :firstName")
	Page<EmployeeResource> findByFirstName(@Param("firstName") String firstName, Pageable pageable);
}
