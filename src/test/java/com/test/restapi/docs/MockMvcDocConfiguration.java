package com.test.restapi.docs;

import static capital.scalable.restdocs.jackson.JacksonResultHandlers.prepareJackson;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.limitJsonArrayLength;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.replaceBinaryContent;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.ManualRestDocumentation;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.section.SectionBuilder;

@TestConfiguration
public class MockMvcDocConfiguration {
	private static final String DEFAULT_AUTHORIZATION = "Resource is public.";

	@Autowired
	ObjectMapper objectMapper;

	
	@Bean
	DocMockMvcBuilderCustomizer docMockMvcBuilderCustomizer() {
		return new DocMockMvcBuilderCustomizer();
	}
	@Bean
	RestDocumentationContextProvider restDocumentation() {
		return new ManualRestDocumentation();
	}
	
	class DocMockMvcBuilderCustomizer implements MockMvcBuilderCustomizer {

		@Override
		public void customize(ConfigurableMockMvcBuilder<?> builder) {
			builder.alwaysDo(
					prepareJackson(objectMapper /*
							 * , new TypeMapping() .mapSubtypes(Metadata.class, Metadata3.class)
							 */))
					.alwaysDo(commonDocumentation()).apply(springSecurity())
					.apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation()).uris().withScheme("https")
					.withHost("rest-api-server").withPort(443).and().snippets()
					.withDefaults(CliDocumentation.curlRequest(), HttpDocumentation.httpRequest(),
							HttpDocumentation.httpResponse(), AutoDocumentation.requestFields(),
							AutoDocumentation.responseFields(), AutoDocumentation.pathParameters(),
							AutoDocumentation.requestParameters(), AutoDocumentation.description(),
							AutoDocumentation.methodAndPath(),
							AutoDocumentation.authorization(DEFAULT_AUTHORIZATION),
							AutoDocumentation.sectionBuilder()
									.snippetNames(new ArrayList<>(SectionBuilder.DEFAULT_SNIPPETS)).skipEmpty(true)
									.build()));
		}
		
		protected RestDocumentationResultHandler commonDocumentation(Snippet... snippets) {
			return document("{class-name}/{method-name}", commonResponsePreprocessor(), snippets);
		}

		protected OperationResponsePreprocessor commonResponsePreprocessor() {
			return preprocessResponse(replaceBinaryContent(), limitJsonArrayLength(objectMapper), prettyPrint());
		}
	}
	
}
