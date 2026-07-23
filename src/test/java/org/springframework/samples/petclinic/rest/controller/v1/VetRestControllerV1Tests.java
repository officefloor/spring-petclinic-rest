package org.springframework.samples.petclinic.rest.controller.v1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
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
 * Integration tests for the /api/vets endpoints, driven end-to-end
 * through the running application against real repositories.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VetRestControllerV1Tests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private VetRepository vetRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    private Vet newVet(String lastName) {
        // VetDto.lastName is validated against a letters/spaces/hyphens/apostrophes pattern,
        // so uniqueness suffixes must not use digits (unlike Owner's lastName, which has no such pattern).
        Vet vet = new Vet();
        vet.setFirstName("James");
        vet.setLastName(lastName);
        vetRepository.save(vet);
        return vet;
    }

    private Specialty newSpecialty(String name) {
        Specialty specialty = new Specialty();
        specialty.setName(name);
        specialtyRepository.save(specialty);
        return specialty;
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void getVetSuccess() throws Exception {
        Vet vet = newVet("Carter");
        mvc.perform(get("/api/vets/" + vet.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(vet.getId()))
            .andExpect(jsonPath("$.firstName").value("James"));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void getVetNotFound() throws Exception {
        mvc.perform(get("/api/vets/999999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getVetForbiddenForOwnerAdmin() throws Exception {
        Vet vet = newVet("Carter");
        mvc.perform(get("/api/vets/" + vet.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void listVetsSuccess() throws Exception {
        Vet vet = newVet("Leary");
        mvc.perform(get("/api/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + vet.getId() + ")].lastName").value(vet.getLastName()));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void createVetSuccess() throws Exception {
        String body = """
            {"firstName":"James","lastName":"Carter","specialties":[]}
            """;
        mvc.perform(post("/api/vets").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/vets/")))
            .andExpect(jsonPath("$.firstName").value("James"));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void createVetResolvesNamedSpecialtiesToManagedEntities() throws Exception {
        Specialty specialty = newSpecialty("radiology-" + System.nanoTime());
        String body = """
            {"firstName":"James","lastName":"Carter","specialties":[{"name":"%s"}]}
            """.formatted(specialty.getName());

        String location = mvc.perform(post("/api/vets").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.specialties[0].id").value(specialty.getId()))
            .andExpect(jsonPath("$.specialties[0].name").value(specialty.getName()))
            .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.specialties[0].id").value(specialty.getId()));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void createVetValidationError() throws Exception {
        String body = """
            {"lastName":"Carter","specialties":[]}
            """;
        mvc.perform(post("/api/vets").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createVetForbiddenForOwnerAdmin() throws Exception {
        String body = """
            {"firstName":"James","lastName":"Carter","specialties":[]}
            """;
        mvc.perform(post("/api/vets").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void updateVetSuccess() throws Exception {
        Vet vet = newVet("Carter");
        String body = """
            {"firstName":"JamesI","lastName":"Carter","specialties":[]}
            """;

        mvc.perform(put("/api/vets/" + vet.getId()).content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/vets/" + vet.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("JamesI"));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void updateVetResolvesNamedSpecialtiesToManagedEntities() throws Exception {
        Vet vet = newVet("Carter");
        Specialty specialty = newSpecialty("dentistry-" + System.nanoTime());
        String body = """
            {"firstName":"James","lastName":"Carter","specialties":[{"name":"%s"}]}
            """.formatted(specialty.getName());

        mvc.perform(put("/api/vets/" + vet.getId()).content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/vets/" + vet.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.specialties[0].id").value(specialty.getId()))
            .andExpect(jsonPath("$.specialties[0].name").value(specialty.getName()));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void updateVetValidationErrorBeforeNotFoundCheck() throws Exception {
        String body = """
            {"lastName":"Carter","specialties":[]}
            """;
        mvc.perform(put("/api/vets/999999").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void updateVetNotFound() throws Exception {
        String body = """
            {"firstName":"Ghost","lastName":"Carter","specialties":[]}
            """;
        mvc.perform(put("/api/vets/999999").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void deleteVetSuccess() throws Exception {
        Vet vet = newVet("ToDelete");
        mvc.perform(delete("/api/vets/" + vet.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void deleteVetNotFound() throws Exception {
        mvc.perform(delete("/api/vets/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void deleteVetForbiddenForOwnerAdmin() throws Exception {
        Vet vet = newVet("Carter");
        mvc.perform(delete("/api/vets/" + vet.getId()))
            .andExpect(status().isForbidden());
    }
}
