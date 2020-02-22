package com.test.restapi.repository.dozer.employee;

import java.util.List;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.dozer.repository.DozerRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.test.restapi.entity.dozer.employee.EmployeeResource;

/**
 * REST API endpoint to manage employee resources
 * 
 * @author kchobantonov
 */
@RepositoryRestResource(collectionResourceRel = "employees", itemResourceRel = "employee", path = "employees")
public interface EmployeeResourceRepository extends DozerRepository<EmployeeResource, Integer> {

	/**
	 * Creates a new employee
	 * 
	 * @param employee the details of the new employee
	 * @return the employee details after creating
	 */
	@Override
	<S extends EmployeeResource> S save(S employee);

	@Query("SELECT e FROM Employee e WHERE e.firstName LIKE :firstName")
	Page<EmployeeResource> findByFirstNameAndReturnPage(@Param("firstName") String firstName, Pageable pageable);

	@Query("SELECT e FROM Employee e WHERE e.firstName LIKE :firstName")
	List<EmployeeResource> findByFirstNameAndReturnList(@Param("firstName") String firstName, Pageable pageable);

	@Query("SELECT e FROM Employee e WHERE e.firstName LIKE :firstName")
	Slice<EmployeeResource> findByFirstNameAndReturnSlice(@Param("firstName") String firstName, Pageable pageable);

	// TODO check the issue with transaction not been enabled
	@Transactional
	@Query("SELECT e FROM Employee e WHERE e.firstName LIKE :firstName")
	Stream<EmployeeResource> findByFirstNameAndReturnStream(@Param("firstName") String firstName, Pageable pageable);

	@Query("SELECT e FROM Employee e WHERE e.id = :id")
	EmployeeResource findByCustomId(@Param("id") Integer id);

	// projections

	@Query("SELECT e.firstName FROM Employee e WHERE e.id = :id")
	String findFirstNameByCustomId(@Param("id") Integer id);

}
