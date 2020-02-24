package com.test.restapi.entity.dozer.employee;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.dozer.annotation.DozerEntity;
import org.springframework.validation.annotation.Validated;

import com.test.restapi.entity.jpa.employee.Employee;
import com.test.restapi.entity.jpa.employee.Gender;

import lombok.Data;

/**
 * REST API resource to represent an Employee
 */
@DozerEntity(adaptedDomainClass = Employee.class)
@Data
@Validated
public class EmployeeResource {
	/**
	 * Unique identifier of the employee
	 */
	@Id
	private Integer id;

	/**
	 * Gender of the employee
	 */
	private Gender gender;

	/**
	 * First name of the employee
	 */
	@NotBlank
	private String firstName;

	/**
	 * Last name of the employee
	 */
	@NotBlank
	@Size(max = 50)
	private String lastName;

	/**
	 * Salary of the employee
	 */
	private Double salary;

	/**
	 * The employee's manager
	 */
	@Reference
	@Valid
	private EmployeeResource manager;

}
