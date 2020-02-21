package com.test.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.test.restapi.entity.dozer.employee.EmployeeResource;
import com.test.restapi.entity.jpa.employee.Gender;

@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class EmployeeResourceTest extends MockMvcBase {
	private EmployeeResource item = new EmployeeResource();

	public EmployeeResourceTest() {
		item.setFirstName("John");
		item.setLastName("Doe");
		item.setGender(Gender.Male);
	}

	@Test
	@Order(1)
	public void getAllEmployees() throws Exception {
		mockMvc.perform(get(getResourceCollectionPath(EmployeeResource.class))).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.employees").isArray());
	}

	@Test
	@Order(2)
	public void searchEmpoyees() throws Exception {
		mockMvc.perform(get(getSearchResourcePath(EmployeeResource.class))).andExpect(status().isOk())
				.andExpect(jsonPath("$._links").isMap())

				.andDo(document("{class-name}/search", commonResponsePreprocessor()));
	}

	@Test
	@Order(3)
	public void addEmployee() throws Exception {

		MvcResult result = mockMvc
				.perform(post(getResourceCollectionPath(EmployeeResource.class)).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(item)))
				.andExpect(status().isCreated()).andExpect(header().exists("location")).andReturn();

		item.setId(getIdFromLocation(EmployeeResource.class, result.getResponse()));
	}

	@Test
	@Order(4)
	public void getEmployee() throws Exception {
		findOneEmployee();
	}

	private EntityModel<EmployeeResource> findOneEmployee() throws Exception {
		MvcResult result = mockMvc.perform(get(getResourceItemPath(EmployeeResource.class), item.getId()))
				.andExpect(status().isOk()).andReturn();

		EntityModel<EmployeeResource> model = toResourceEntity(new TypeReference<EntityModel<EmployeeResource>>() {
		}, result.getResponse());

		assertEquals(objectMapper.writeValueAsString(item), objectMapper.writeValueAsString(model.getContent()));

		return model;
	}

	@Test
	@Order(5)
	public void updateEmployee() throws Exception {
		item.setFirstName("Jane");
		item.setGender(Gender.Female);

		mockMvc.perform(patch(getResourceItemPath(EmployeeResource.class), item.getId())
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(item)))
				.andExpect(status().isNoContent());

		EntityModel<EmployeeResource> model = findOneEmployee();

		assertEquals(objectMapper.writeValueAsString(item), objectMapper.writeValueAsString(model.getContent()));
	}

	@Test
	@Order(6)
	public void deleteEmployee() throws Exception {
		mockMvc.perform(delete(getResourceItemPath(EmployeeResource.class), item.getId()))
				.andExpect(status().isNoContent());
	}

}
