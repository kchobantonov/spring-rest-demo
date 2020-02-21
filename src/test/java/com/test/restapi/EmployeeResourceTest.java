package com.test.restapi;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class EmployeeResourceTest extends MockMvcBase
{
    @Test
    public void getItem() throws Exception {
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.description", is("main item")))
                .andExpect(jsonPath("$.attributes.text", is("first item")))
                .andExpect(jsonPath("$.attributes.number", is(1)))
                .andExpect(jsonPath("$.attributes.bool", is(true)))
                .andExpect(jsonPath("$.attributes.decimal", is(1.11)))
                .andExpect(jsonPath("$.attributes.amount", is("3.14 EUR")))
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children", hasSize(1)))
                .andExpect(jsonPath("$.children[0].id", is("child-1")))
                .andExpect(jsonPath("$.children[0].description", is("first child")))
                .andExpect(jsonPath("$.children[0].attributes", nullValue()))
                .andExpect(jsonPath("$.children[0].children", nullValue()));
    }

    @Test
    public void getAllItems() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", hasItems("1", "child-1")));
    }

    @Test
    public void addItem() throws Exception {
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Hot News\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", is("/items/2")));
    }

    @Test
    public void updateItem() throws Exception {
        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Hot News\"}"))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.description", is("Hot News")))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteItem() throws Exception {
        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void getChildItem() throws Exception {
        mockMvc.perform(get("/items/1/child-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("child-1")))
                .andExpect(jsonPath("$.description", is("first child")));
    }

    @Test
    public void searchEmpoyees() throws Exception {
        mockMvc.perform(get("/employees/search")
                .param("desc", "main")
                .param("hint", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is("1")))
                .andExpect(jsonPath("$.content[0].description", is("main item")))
                // example for overriding path and preprocessors
                .andDo(document("{class-name}/search", commonResponsePreprocessor()));
    }
}
