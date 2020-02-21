package com.test.restapi.entity.dozer.employee;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.dozer.annotation.DozerEntity;

import com.test.restapi.entity.jpa.employee.Employee;
import com.test.restapi.entity.jpa.employee.Gender;

import lombok.Data;

@DozerEntity( adaptedDomainClass = Employee.class )
@Data
public class EmployeeResource
{
    @Id
    private Integer          id;

    private Gender           gender;

    @NotBlank
    private String           firstName;
    
    @NotBlank
    @Size(max = 50)
    private String           lastName;
    
    private Double           salary;

    @Reference
    private EmployeeResource manager;

}
