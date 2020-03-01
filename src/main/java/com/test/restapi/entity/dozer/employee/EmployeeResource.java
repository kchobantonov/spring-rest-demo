package com.test.restapi.entity.dozer.employee;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.validation.annotation.Validated;

import com.test.restapi.entity.jpa.employee.Gender;
import com.test.restapi.validation.HttpMethodValidationGroup;

import lombok.Data;

/**
 * REST API resource to represent an Employee
 */
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
	@NotBlank(groups = HttpMethodValidationGroup.POST.class)
	private String firstName;

	/**
	 * Last name of the employee
	 */
	@NotBlank(groups = HttpMethodValidationGroup.PATCH.class)
	@Size(max = 50)
	private String lastName;

	/**
	 * Salary of the employee
	 */
	private Double salary;

}
