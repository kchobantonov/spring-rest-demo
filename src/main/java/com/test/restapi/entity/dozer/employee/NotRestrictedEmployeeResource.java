package com.test.restapi.entity.dozer.employee;

import javax.validation.Valid;

import org.springframework.data.annotation.Reference;
import org.springframework.data.dozer.annotation.DozerEntity;

import com.test.restapi.entity.jpa.employee.Employee;

import lombok.Data;
import lombok.EqualsAndHashCode;

@DozerEntity(adaptedDomainClass = Employee.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class NotRestrictedEmployeeResource extends EmployeeResource {

	/**
	 * The employee's manager
	 */
	@Reference
	@Valid
	private EmployeeResource manager;

}
