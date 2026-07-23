package org.springframework.samples.petclinic.rest.controller.v1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the /api/specialties endpoints, driven end-to-end
 * through the running application against real repositories.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SpecialtyRestControllerV1Tests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    private Specialty newSpecialty(String name) {
        Specialty specialty = new Specialty();
        specialty.setName(name);
        specialtyRepository.save(specialty);
        return specialty;
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void getSpecialtySuccess() throws Exception {
        Specialty specialty = newSpecialty("radiology-" + System.nanoTime());
        mvc.perform(get("/api/specialties/" + specialty.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(specialty.getId()))
            .andExpect(jsonPath("$.name").value(specialty.getName()));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void getSpecialtyNotFound() throws Exception {
        mvc.perform(get("/api/specialties/999999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getSpecialtyForbiddenForOwnerAdmin() throws Exception {
        Specialty specialty = newSpecialty("radiology-" + System.nanoTime());
        mvc.perform(get("/api/specialties/" + specialty.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void listSpecialtiesSuccess() throws Exception {
        Specialty specialty = newSpecialty("dentistry-" + System.nanoTime());
        mvc.perform(get("/api/specialties").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + specialty.getId() + ")].name").value(specialty.getName()));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void createSpecialtySuccess() throws Exception {
        String body = """
            {"name":"surgery-%d"}
            """.formatted(System.nanoTime());
        mvc.perform(post("/api/specialties").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/specialties/")))
            .andExpect(jsonPath("$.name").value(org.hamcrest.Matchers.startsWith("surgery-")));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void createSpecialtyValidationError() throws Exception {
        String body = """
            {"name":""}
            """;
        mvc.perform(post("/api/specialties").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createSpecialtyForbiddenForOwnerAdmin() throws Exception {
        String body = """
            {"name":"surgery-%d"}
            """.formatted(System.nanoTime());
        mvc.perform(post("/api/specialties").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void updateSpecialtySuccess() throws Exception {
        Specialty specialty = newSpecialty("dentistry-" + System.nanoTime());
        String body = """
            {"name":"dentistry I"}
            """;

        mvc.perform(put("/api/specialties/" + specialty.getId()).content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/specialties/" + specialty.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("dentistry I"));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void updateSpecialtyValidationErrorBeforeNotFoundCheck() throws Exception {
        String body = """
            {"name":""}
            """;
        mvc.perform(put("/api/specialties/999999").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void updateSpecialtyNotFound() throws Exception {
        String body = """
            {"name":"Ghost"}
            """;
        mvc.perform(put("/api/specialties/999999").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void deleteSpecialtySuccess() throws Exception {
        Specialty specialty = newSpecialty("ToDelete-" + System.nanoTime());
        mvc.perform(delete("/api/specialties/" + specialty.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void deleteSpecialtyNotFound() throws Exception {
        mvc.perform(delete("/api/specialties/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void deleteSpecialtyForbiddenForOwnerAdmin() throws Exception {
        Specialty specialty = newSpecialty("dentistry-" + System.nanoTime());
        mvc.perform(delete("/api/specialties/" + specialty.getId()))
            .andExpect(status().isForbidden());
    }
}
