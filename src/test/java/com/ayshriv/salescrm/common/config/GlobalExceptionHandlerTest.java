package com.ayshriv.salescrm.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testMalformedJson() throws Exception {
        mockMvc.perform(post("/api/test/filter-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusType").value("ERROR"))
                .andExpect(jsonPath("$.text").value("Malformed JSON request body."));
    }

    @Test
    void testNotFoundRoute() throws Exception {
        mockMvc.perform(get("/api/test/non-existent-route-12345"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusType").value("ERROR"))
                .andExpect(jsonPath("$.text").value("Requested endpoint or resource not found."));
    }
}
