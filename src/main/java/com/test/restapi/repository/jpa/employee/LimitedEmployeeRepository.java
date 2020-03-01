package com.test.restapi.repository.jpa.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.test.restapi.entity.jpa.employee.Employee;

@Repository
public interface LimitedEmployeeRepository extends JpaRepository<Employee, Integer> {
	@Override
	@Query(value = "select e from Employee e where e.salary < 400")
	Page<Employee> findAll(Pageable pageable);

	@Override
	@Query(value = "select e from Employee e where e.id = :id and e.salary < 400")
	Optional<Employee> findById(@Param("id") Integer id);

	@Override
	@Query(value = "select e from Employee e where e.salary < 400")
	List<Employee> findAll(Sort sort);
}
