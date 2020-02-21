package com.test.restapi;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.SnippetRegistry;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import static capital.scalable.restdocs.AutoDocumentation.authorization;
import static capital.scalable.restdocs.AutoDocumentation.description;
import static capital.scalable.restdocs.AutoDocumentation.embedded;
import static capital.scalable.restdocs.AutoDocumentation.links;
import static capital.scalable.restdocs.AutoDocumentation.methodAndPath;
import static capital.scalable.restdocs.AutoDocumentation.pathParameters;
import static capital.scalable.restdocs.AutoDocumentation.requestFields;
import static capital.scalable.restdocs.AutoDocumentation.requestParameters;
import static capital.scalable.restdocs.AutoDocumentation.responseFields;
import static capital.scalable.restdocs.AutoDocumentation.section;
import static capital.scalable.restdocs.jackson.JacksonResultHandlers.prepareJackson;
import static capital.scalable.restdocs.misc.AuthorizationSnippet.documentAuthorization;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.limitJsonArrayLength;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.replaceBinaryContent;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(
{
        RestDocumentationExtension.class, SpringExtension.class
} )
@WithMockUser( username = "demo", password = "demo", roles =
{
        "USER", "ADMIN"
} )
@SpringBootTest
public class MockMvcBase
{
    @Autowired
    private WebApplicationContext              context;

    @Autowired
    protected ObjectMapper                     objectMapper;

    @Autowired
    protected RestDocumentationContextProvider restDocumentation;

    protected MockMvc                          mockMvc;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.mockMvc = MockMvcBuilders.webAppContextSetup( context )
                .alwaysDo( prepareJackson( objectMapper /*
                                                         * , new TypeMapping()
                                                         * .mapSubtypes(Metadata.class,
                                                         * Metadata3.class)
                                                         */) )
                .alwaysDo(commonDocumentation())
                .apply( springSecurity() )
                .apply( MockMvcRestDocumentation.documentationConfiguration( restDocumentation ).uris()
                        .withScheme( "http" ).withHost( "localhost" ).withPort( 8080 ).and().snippets()
                        .withDefaults( CliDocumentation.curlRequest(), HttpDocumentation.httpRequest(),
                                       HttpDocumentation.httpResponse(), AutoDocumentation.requestFields(),
                                       AutoDocumentation.responseFields(), AutoDocumentation.pathParameters(),
                                       AutoDocumentation.requestParameters(), AutoDocumentation.description(),
                                       AutoDocumentation.methodAndPath(),
                                       AutoDocumentation.sectionBuilder()
                                               .snippetNames( SnippetRegistry.PATH_PARAMETERS,
                                                              SnippetRegistry.REQUEST_PARAMETERS,
                                                              SnippetRegistry.REQUEST_FIELDS,
                                                              SnippetRegistry.RESPONSE_FIELDS )
                                               .skipEmpty( true ).build() ) )
                .build();
    }

    protected RestDocumentationResultHandler commonDocumentation(Snippet... snippets) {
        return document("{class-name}/{method-name}", commonResponsePreprocessor(), snippets);
    }

    protected OperationResponsePreprocessor commonResponsePreprocessor() {
        return preprocessResponse(replaceBinaryContent(), limitJsonArrayLength(objectMapper),
                prettyPrint());
    }

}
