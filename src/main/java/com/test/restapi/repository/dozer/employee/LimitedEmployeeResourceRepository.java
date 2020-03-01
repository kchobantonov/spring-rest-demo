package com.test.restapi.repository.dozer.employee;

import org.springframework.data.dozer.annotation.DozerRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.test.restapi.entity.dozer.employee.LimitedEmployeeResource;
import com.test.restapi.repository.dozer.ValidatedDozerRepository;
import com.test.restapi.repository.jpa.employee.LimitedEmployeeRepository;

/**
 * REST API endpoint to manage employee resources
 * 
 * @author kchobantonov
 */
@DozerRepository(adaptedRepositoryClass = LimitedEmployeeRepository.class)
@RepositoryRestResource(collectionResourceRel = "employees", itemResourceRel = "employee", path = "employees")
public interface LimitedEmployeeResourceRepository extends ValidatedDozerRepository<LimitedEmployeeResource, Integer> {


}
