package com.gam.api.controller;

import com.gam.api.config.WebMvcConfig;
import com.gam.api.config.json.CustomObjectMapper;
import com.gam.api.domain.test.controller.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(controllers = TestController.class)
public class HealthCheckControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void healthCheckReturn() throws Exception {
        String WELCOME_STRING = "Hello gam Server!";

        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(WELCOME_STRING));
    }
}
