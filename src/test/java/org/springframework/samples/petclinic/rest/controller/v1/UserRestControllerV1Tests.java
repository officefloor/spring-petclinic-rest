package org.springframework.samples.petclinic.rest.controller.v1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the /api/users endpoint, driven end-to-end
 * through the running application against real repositories.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserRestControllerV1Tests {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserSuccess() throws Exception {
        // users.username is VARCHAR(20), so uniqueness suffixes must stay short (unlike Owner's lastName column).
        String body = """
            {"username":"newuser1","password":"password","enabled":true,"roles":[{"name":"OWNER_ADMIN"}]}
            """;
        mvc.perform(post("/api/users").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.roles[0].name").value("ROLE_OWNER_ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserPrefixesAlreadyPrefixedRoleNamesIdempotently() throws Exception {
        String body = """
            {"username":"newuser2","password":"password","enabled":true,"roles":[{"name":"ROLE_VET_ADMIN"}]}
            """;
        mvc.perform(post("/api/users").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.roles[0].name").value("ROLE_VET_ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserValidationErrorOnEmptyUsername() throws Exception {
        String body = """
            {"username":"","password":"password","enabled":true,"roles":[{"name":"OWNER_ADMIN"}]}
            """;
        mvc.perform(post("/api/users").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserNoRolesFails() throws Exception {
        String body = """
            {"username":"newuser3","password":"password","enabled":true,"roles":[]}
            """;
        mvc.perform(post("/api/users").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createUserForbiddenForOwnerAdmin() throws Exception {
        String body = """
            {"username":"newuser4","password":"password","enabled":true,"roles":[{"name":"OWNER_ADMIN"}]}
            """;
        mvc.perform(post("/api/users").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }
}
