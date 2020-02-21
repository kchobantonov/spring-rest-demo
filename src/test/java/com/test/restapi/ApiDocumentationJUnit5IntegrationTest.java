package com.test.restapi;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.test.restapi.entity.dozer.employee.EmployeeResource;
import com.test.restapi.entity.jpa.employee.Gender;

@ExtendWith(
{
        RestDocumentationExtension.class, SpringExtension.class
} )
@WithMockUser( username = "demo", password = "demo", roles =
{
        "USER", "ADMIN"
} )
@SpringBootTest
public class ApiDocumentationJUnit5IntegrationTest
{
    @Autowired
    private ObjectMapper          objectMapper;

    @Autowired
    private WebApplicationContext context;

    private MockMvc               mockMvc;

    //@BeforeEach
    public void setUp( WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation )
    {
        this.mockMvc = MockMvcBuilders.webAppContextSetup( this.context )
                .apply( documentationConfiguration( restDocumentation ).uris().withHost( "rest-api" )
                        .withScheme( "https" ).withPort( 443 ) )
                .apply( springSecurity() )
                .alwaysDo( document( "{method-name}/{step}/", preprocessRequest( prettyPrint() ),
                                     preprocessResponse( prettyPrint() ) ) )
                .build();
    }

    //@Test
    public void index() throws Exception
    {
        this.mockMvc.perform( get( "/" ).accept( MediaTypes.HAL_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "_links.employees", is( notNullValue() ) ) )
                .andExpect( jsonPath( "_links.people", is( notNullValue() ) ) )
                .andExpect( jsonPath( "_links.profile", is( notNullValue() ) ) );
    }

    //@Test
    public void creatingAEmployee() throws JsonProcessingException, Exception
    {
        String noteLocation = createEmployee();
        MvcResult employee = getEmployee( noteLocation );
    }

    String createEmployee() throws Exception
    {
        EmployeeResource employee = new EmployeeResource();
        employee.setFirstName( "John" );
        employee.setLastName( "Doe" );
        employee.setGender( Gender.Male );

        String employeeLocation = this.mockMvc
                .perform( post( "/notes" ).contentType( MediaTypes.HAL_JSON )
                        .content( objectMapper.writeValueAsString( employee ) ) )
                .andExpect( status().isCreated() ).andExpect( header().string( "Location", notNullValue() ) )
                .andReturn().getResponse().getHeader( "Location" );
        return employeeLocation;
    }
    
    MvcResult getEmployee(String employeeLocation) throws Exception {
        return this.mockMvc.perform(get(employeeLocation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is(notNullValue())))
                .andExpect(jsonPath("body", is(notNullValue())))
                .andExpect(jsonPath("_links.self", is(notNullValue())))
                .andReturn();
    }

    private String getLink(MvcResult result, String rel)
            throws UnsupportedEncodingException {
        return JsonPath.parse(result.getResponse().getContentAsString()).read(
                "_links." + rel + ".href");
    }

}
