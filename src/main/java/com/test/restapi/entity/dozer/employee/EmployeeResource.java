package com.test.restapi.entity.dozer.employee;

import org.springframework.data.annotation.Id;
import org.springframework.data.dozer.annotation.DozerEntity;

import com.test.restapi.entity.jpa.employee.Employee;

import lombok.Data;

@DozerEntity(adaptedDomainClass = Employee.class)
@Data
public class EmployeeResource {
	@Id
	private Integer id;
	
}
