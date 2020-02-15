package com.test.restapi.dbinit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.test.restapi.entity.jpa.employee.Employee;
import com.test.restapi.entity.jpa.employee.Gender;
import com.test.restapi.repository.jpa.employee.EmployeeRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmployeeInitializer {
	private static final String[] MALE_FIRST_NAMES = { "Jacob", "Ethan", "Michael", "Alexander", "William", "Joshua",
			"Daniel", "Jayden", "Noah", "Anthony" };
	private static final String[] FEMALE_FIRST_NAMES = { "Isabella", "Emma", "Olivia", "Sophia", "Ava", "Emily",
			"Madison", "Abigail", "Chloe", "Mia" };
	private static final String[] LAST_NAMES = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis",
			"Garcia", "Rodriguez", "Wilson" };

	@Autowired
	public EmployeeInitializer(EmployeeRepository repository) throws Exception {

		if (repository.count() != 0) {
			return;
		}

		List<Employee> employees = createEmployees(5);

		log.info("Importing {} employees into JPA store…", employees.size());
		repository.saveAll(employees);
		log.info("Successfully imported {} employees.", repository.count());
	}

	public List<Employee> createEmployees(int quantity) {
		List<Employee> result = new ArrayList<Employee>();
		for (int index = 0; index < quantity; index++) {
			result.add(createRandomEmployee());
		}

		return result;
	}

	public Employee createRandomEmployee() {
		Random r = new Random();

		Employee emp = new Employee();
		emp.setGender(Gender.values()[r.nextInt(2)]);
		if (Gender.Male.equals(emp.getGender())) {
			emp.setFirstName(MALE_FIRST_NAMES[r.nextInt(MALE_FIRST_NAMES.length)]);
		} else {
			emp.setFirstName(FEMALE_FIRST_NAMES[r.nextInt(FEMALE_FIRST_NAMES.length)]);
		}
		emp.setLastName(LAST_NAMES[r.nextInt(LAST_NAMES.length)]);
		emp.addPhoneNumber("HOME", "111", "5552222");
		emp.addPhoneNumber("WORK", "222", "5552222");

		return emp;
	}
}
