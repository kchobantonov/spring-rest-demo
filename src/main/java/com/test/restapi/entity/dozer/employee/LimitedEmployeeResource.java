package com.test.restapi.entity.dozer.employee;

import org.springframework.data.dozer.annotation.DozerEntity;

import com.test.restapi.entity.jpa.employee.Employee;

import lombok.Data;
import lombok.EqualsAndHashCode;

@DozerEntity(adaptedDomainClass = Employee.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class LimitedEmployeeResource extends EmployeeResource {

}
