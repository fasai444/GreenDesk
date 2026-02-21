package org.example.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("HomeController – redirection racine")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void root_should_redirect_to_home_html() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home.html"));
    }

    @Test
    void home_html_should_exist_or_be_served() throws Exception {
        mockMvc.perform(get("/home.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    void unknown_path_should_return_404() throws Exception {
        mockMvc.perform(get("/nimportequoi"))
                .andExpect(status().isNotFound());
    }
}