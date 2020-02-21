package com.test.restapi;

import static capital.scalable.restdocs.jackson.JacksonResultHandlers.prepareJackson;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.limitJsonArrayLength;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.replaceBinaryContent;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.core.mapping.SearchResourceMappings;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.SnippetRegistry;

@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@WithMockUser(username = "demo", password = "demo", roles = { "USER", "ADMIN" })
@SpringBootTest
public class MockMvcBase {
	@Autowired
	private WebApplicationContext context;

	@Autowired
	protected ObjectMapper objectMapper;

	/// @Autowired
	// protected RestDocumentationContextProvider restDocumentation;

	protected MockMvc mockMvc;

	@Autowired
	protected RepositoryResourceMappings mappings;

	@Autowired
	protected RepositoryRestConfiguration configuration;

	@Autowired
	protected PersistentEntities persistentEntities;

	@Autowired
	@Qualifier("defaultConversionService")
	protected ConversionService conversionService;

	protected ResourceMetadata getResourceMetadata(Class resourceClass) {
		return mappings.getMetadataFor(resourceClass);
	}

	protected <T> EntityModel<T> toResourceEntity(TypeReference<EntityModel<T>> type, MockHttpServletResponse response)
			throws UnsupportedEncodingException, JsonMappingException, JsonProcessingException {
		String content = response.getContentAsString();

		return objectMapper.readValue(content, type);
	}

	protected <ID> ID getIdFromLocation(Class<?> resourceClass, MockHttpServletResponse response) {
		String location = response.getHeader("location");
		String id = location.substring(location.lastIndexOf("/") + 1, location.length());

		Class<ID> idType = (Class<ID>) persistentEntities.getRequiredPersistentEntity(resourceClass)
				.getRequiredIdProperty().getActualType();

		return conversionService.convert(id, idType);
	}

	protected String getResourceCollectionPath(Class resourceClass) {
		return configuration.getBasePath().getPath() + getResourceMetadata(resourceClass).getPath().toString();
	}

	protected String getResourceItemPath(Class resourceClass) {
		return getResourceCollectionPath(resourceClass) + "/{id}";
	}

	protected String getSearchResourcePath(Class resourceClass) {
		return getResourceCollectionPath(resourceClass) + getSearchMapping(resourceClass).getPath().toString();
	}

	protected SearchResourceMappings getSearchMapping(Class resourceClass) {
		return mappings.getSearchResourceMappings(resourceClass);
	}

	@BeforeEach
	public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation)
			throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.alwaysDo(
						prepareJackson(objectMapper /*
													 * , new TypeMapping() .mapSubtypes(Metadata.class, Metadata3.class)
													 */))
				.alwaysDo(commonDocumentation()).apply(springSecurity())
				.apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation).uris().withScheme("http")
						.withHost("localhost").withPort(8080).and().snippets()
						.withDefaults(CliDocumentation.curlRequest(), HttpDocumentation.httpRequest(),
								HttpDocumentation.httpResponse(), AutoDocumentation.requestFields(),
								AutoDocumentation.responseFields(), AutoDocumentation.pathParameters(),
								AutoDocumentation.requestParameters(), AutoDocumentation.description(),
								AutoDocumentation.methodAndPath(),
								AutoDocumentation.sectionBuilder()
										.snippetNames(SnippetRegistry.PATH_PARAMETERS,
												SnippetRegistry.REQUEST_PARAMETERS, SnippetRegistry.REQUEST_FIELDS,
												SnippetRegistry.RESPONSE_FIELDS)
										.skipEmpty(true).build()))
				.build();
	}

	protected RestDocumentationResultHandler commonDocumentation(Snippet... snippets) {
		return document("{class-name}/{method-name}", commonResponsePreprocessor(), snippets);
	}

	protected OperationResponsePreprocessor commonResponsePreprocessor() {
		return preprocessResponse(replaceBinaryContent(), limitJsonArrayLength(objectMapper), prettyPrint());
	}

}
