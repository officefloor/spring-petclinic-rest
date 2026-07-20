package org.springframework.samples.petclinic.rest.controller.v1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the context-root redirect, driven against the OfficeFloor REST YAML
 * pipeline rather than a Spring MVC controller.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RootRestControllerV1Tests {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser
    void redirectsToSwaggerUi() throws Exception {
        // test/resources sets petclinic.security.enable=true, so the base filter chain requires
        // authentication on every request regardless of this endpoint's own (lack of) authorize rule.
        mvc.perform(get("/"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "swagger-ui/index.html"));
    }
}
