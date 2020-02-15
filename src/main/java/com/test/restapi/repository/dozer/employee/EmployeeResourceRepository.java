package com.test.restapi.repository.dozer.employee;

import org.springframework.data.dozer.repository.DozerRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.test.restapi.entity.dozer.employee.EmployeeResource;

@RepositoryRestResource(collectionResourceRel = "employees", itemResourceRel = "employee", path = "dozeremployees")
public interface EmployeeResourceRepository extends DozerRepository<EmployeeResource, Integer> {

}
