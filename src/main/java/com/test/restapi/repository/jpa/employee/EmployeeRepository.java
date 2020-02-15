package com.test.restapi.repository.jpa.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.test.restapi.data.jpa.employee.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

}
