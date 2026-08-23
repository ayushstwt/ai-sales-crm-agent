package com.ayshriv.salescrm.common.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFilteringEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/filter-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value("SUCCESS"))
                .andExpect(jsonPath("$.text").value("Test Entity details fetched successfully."))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.total").doesNotExist());
    }
}
