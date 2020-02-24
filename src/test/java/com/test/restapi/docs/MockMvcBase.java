package com.test.restapi.docs;

import static capital.scalable.restdocs.jackson.JacksonResultHandlers.prepareJackson;
import static capital.scalable.restdocs.misc.AuthorizationSnippet.documentAuthorization;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.limitJsonArrayLength;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.replaceBinaryContent;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;

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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.section.SectionBuilder;

@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@SpringBootTest
public abstract class MockMvcBase<T, ID> {

	private static final String DEFAULT_AUTHORIZATION = "Resource is public.";

	@Autowired
	private WebApplicationContext context;

	@Autowired
	protected ObjectMapper objectMapper;

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

	protected Class<T> resourceClass;

	public MockMvcBase(Class<T> resourceClass) {
		this.resourceClass = resourceClass;
	}

	protected ResourceMetadata getResourceMetadata() {
		return mappings.getMetadataFor(resourceClass);
	}

	protected abstract TypeReference<EntityModel<T>> getTypeReference();

	protected EntityModel<T> toResourceEntity(MockHttpServletResponse response)
			throws UnsupportedEncodingException, JsonMappingException, JsonProcessingException {
		String content = response.getContentAsString();

		return objectMapper.readValue(content, getTypeReference());
	}

	protected ID getId(T resource) {
		Method getIdMethod = persistentEntities.getRequiredPersistentEntity(resourceClass).getRequiredIdProperty()
				.getRequiredGetter();
		ReflectionUtils.makeAccessible(getIdMethod);
		return (ID) ReflectionUtils.invokeMethod(getIdMethod, resource);
	}

	protected void setId(T resource, ID id) {
		Method setIdMethod = persistentEntities.getRequiredPersistentEntity(resourceClass).getRequiredIdProperty()
				.getRequiredSetter();
		ReflectionUtils.makeAccessible(setIdMethod);
		ReflectionUtils.invokeMethod(setIdMethod, resource, id);
	}

	protected ID getIdFromResponse(MockHttpServletResponse response) {
		String location = response.getHeader("location");
		String id = location.substring(location.lastIndexOf("/") + 1, location.length());

		Class<ID> idType = (Class<ID>) persistentEntities.getRequiredPersistentEntity(resourceClass)
				.getRequiredIdProperty().getActualType();

		return conversionService.convert(id, idType);
	}

	protected MockHttpServletRequestBuilder httpGetResources() {
		return get(configuration.getBasePath().getPath() + "{repository}", getResourceMetadata().getPath());
	}

	protected MockHttpServletRequestBuilder httpGetResource(ID id) {
		return get(configuration.getBasePath().getPath() + "{repository}/" + getResourceItemIdParameter(),
				getResourceMetadata().getPath(), id);
	}

	protected MockHttpServletRequestBuilder httpPatchResource(ID id) {
		return patch(configuration.getBasePath().getPath() + "{repository}/" + getResourceItemIdParameter(),
				getResourceMetadata().getPath(), id).contentType(MediaType.APPLICATION_JSON);
	}

	protected MockHttpServletRequestBuilder httpPutResource(ID id) {
		return put(configuration.getBasePath().getPath() + "{repository}/" + getResourceItemIdParameter(),
				getResourceMetadata().getPath(), id).contentType(MediaType.APPLICATION_JSON);
	}

	protected MockHttpServletRequestBuilder httpDeleteResource(ID id) {
		return delete(configuration.getBasePath().getPath() + "{repository}/" + getResourceItemIdParameter(),
				getResourceMetadata().getPath(), id);
	}

	protected MockHttpServletRequestBuilder httpProfileResource() {
		return get(configuration.getBasePath().getPath() + "{profile}{repository}", "/profile",
				getResourceMetadata().getPath()).accept("application/schema+json");
	}

	protected MockHttpServletRequestBuilder httpSearchResources() {
		return get(configuration.getBasePath().getPath() + "{repository}/" + getSearchMapping(resourceClass).getPath(),
				getResourceMetadata().getPath());
	}

	protected MockHttpServletRequestBuilder httpPostResource() {
		return post(configuration.getBasePath().getPath() + "{repository}", getResourceMetadata().getPath())
				.contentType(MediaType.APPLICATION_JSON);
	}

	protected String getResourceCollectionPath(Class resourceClass) {
		return configuration.getBasePath().getPath() + getResourceMetadata().getPath().toString();
	}

	protected String getResourceItemIdParameter() {
		return "{id}";
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
				.apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation).uris().withScheme("https")
						.withHost("rest-api-server").withPort(443).and().snippets()
						.withDefaults(CliDocumentation.curlRequest(), HttpDocumentation.httpRequest(),
								HttpDocumentation.httpResponse(), AutoDocumentation.requestFields(),
								AutoDocumentation.responseFields(), AutoDocumentation.pathParameters(),
								AutoDocumentation.requestParameters(), AutoDocumentation.description(),
								AutoDocumentation.methodAndPath(),
								AutoDocumentation.authorization(DEFAULT_AUTHORIZATION),
								AutoDocumentation.sectionBuilder()
										.snippetNames(new ArrayList<>(SectionBuilder.DEFAULT_SNIPPETS)).skipEmpty(true)
										.build()))
				.build();
	}

	protected RequestPostProcessor userToken(String username, String password) {
		return request -> {
			// If the tests requires setup logic for users, you can place it here.
			// Authorization headers or cookies for users should be added here as well.
			String accessToken;
			try {
				accessToken = getAccessToken(username, password);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			request.addHeader("Authorization", "Basic " + accessToken);
			return documentAuthorization(request, "User access token required.");
		};
	}

	private String getAccessToken(String username, String password) throws Exception {
		byte[] encoded = Base64.getEncoder().encode((username + ":" + password).getBytes());
		return new String(encoded);
	}

	protected RestDocumentationResultHandler commonDocumentation(Snippet... snippets) {
		return document("{class-name}/{method-name}", commonResponsePreprocessor(), snippets);
	}

	protected OperationResponsePreprocessor commonResponsePreprocessor() {
		return preprocessResponse(replaceBinaryContent(), limitJsonArrayLength(objectMapper), prettyPrint());
	}

}