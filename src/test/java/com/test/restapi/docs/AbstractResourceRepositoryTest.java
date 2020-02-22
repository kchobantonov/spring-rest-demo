package com.test.restapi.docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
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
import org.springframework.test.web.servlet.MvcResult;

@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public abstract class AbstractResourceRepositoryTest<T, ID> extends MockMvcBase<T, ID> {
	protected T resource;

	public AbstractResourceRepositoryTest(Class<T> resourceClass, T resource) {
		super(resourceClass);
		this.resource = resource;
	}

	@Test
	@Order(0)
	public void schema() throws Exception {
		mockMvc.perform(httpProfileResource()).andExpect(status().isOk())
				.andDo(document("{class-name}/{method-name}", preprocessResponse(prettyPrint())));
	}

	@Test
	@Order(1)
	public void getResources() throws Exception {
		mockMvc.perform(httpGetResources()).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded." + getResourceMetadata().getRel()).isArray());
	}

	@Test
	@Order(2)
	public void searchResources() throws Exception {
		if (getSearchMapping(resourceClass).isExported()) {
			mockMvc.perform(httpSearchResources()).andExpect(status().isOk()).andExpect(jsonPath("$._links").isMap());
		}
	}

	@Test
	@Order(3)
	public void addResource() throws Exception {

		MvcResult result = mockMvc.perform(httpPostResource().content(objectMapper.writeValueAsString(resource)))
				.andExpect(status().isCreated()).andExpect(header().exists("location")).andReturn();

		ID id = getIdFromResponse(result.getResponse());
		setId(resource, id);
	}

	@Test
	@Order(4)
	public void getResource() throws Exception {
		findOneResource();
	}

	private EntityModel<T> findOneResource() throws Exception {

		MvcResult result = mockMvc.perform(httpGetResource(getId(resource))).andExpect(status().isOk()).andReturn();

		EntityModel<T> model = toResourceEntity(result.getResponse());

		assertEquals(objectMapper.writeValueAsString(resource), objectMapper.writeValueAsString(model.getContent()));

		return model;
	}

	@Test
	@Order(5)
	public void updateResource() throws Exception {
		applyUpdateChanges(resource);

		mockMvc.perform(httpPatchResource(getId(resource)).content(objectMapper.writeValueAsString(resource)))
				.andExpect(status().isNoContent());

		EntityModel<T> model = findOneResource();

		assertEquals(objectMapper.writeValueAsString(resource), objectMapper.writeValueAsString(model.getContent()));
	}

	protected abstract void applyUpdateChanges(T resource);

	@Test
	@Order(6)
	public void deleteResource() throws Exception {
		mockMvc.perform(httpDeleteResource(getId(resource))).andExpect(status().isNoContent());
	}

}
