package org.springframework.samples.petclinic.rest.controller.v1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
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
 * Integration tests for the /api/pettypes endpoints, driven end-to-end
 * through the running application against real repositories.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PetTypeRestControllerV1Tests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PetTypeRepository petTypeRepository;

    private PetType newPetType(String name) {
        PetType type = new PetType();
        type.setName(name);
        petTypeRepository.save(type);
        return type;
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getPetTypeSuccessAsOwnerAdmin() throws Exception {
        PetType type = newPetType("cat-" + System.nanoTime());
        mvc.perform(get("/api/pettypes/" + type.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(type.getId()))
            .andExpect(jsonPath("$.name").value(type.getName()));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void getPetTypeSuccessAsVetAdmin() throws Exception {
        PetType type = newPetType("cat-" + System.nanoTime());
        mvc.perform(get("/api/pettypes/" + type.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(type.getName()));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getPetTypeNotFound() throws Exception {
        mvc.perform(get("/api/pettypes/999999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void listPetTypesSuccessAsOwnerAdmin() throws Exception {
        PetType type = newPetType("dog-" + System.nanoTime());
        mvc.perform(get("/api/pettypes").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + type.getId() + ")].name").value(type.getName()));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void listPetTypesSuccessAsVetAdmin() throws Exception {
        PetType type = newPetType("dog-" + System.nanoTime());
        mvc.perform(get("/api/pettypes").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + type.getId() + ")].name").value(type.getName()));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void createPetTypeSuccess() throws Exception {
        String body = """
            {"name":"snake-%d"}
            """.formatted(System.nanoTime());
        mvc.perform(post("/api/pettypes").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/pettypes/")))
            .andExpect(jsonPath("$.name").value(org.hamcrest.Matchers.startsWith("snake-")));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void createPetTypeValidationError() throws Exception {
        String body = """
            {"name":""}
            """;
        mvc.perform(post("/api/pettypes").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createPetTypeForbiddenForOwnerAdmin() throws Exception {
        String body = """
            {"name":"snake-%d"}
            """.formatted(System.nanoTime());
        mvc.perform(post("/api/pettypes").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void updatePetTypeSuccess() throws Exception {
        PetType type = newPetType("dog-" + System.nanoTime());
        String body = """
            {"id":%d,"name":"dog I"}
            """.formatted(type.getId());

        mvc.perform(put("/api/pettypes/" + type.getId()).content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/pettypes/" + type.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("dog I"));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void updatePetTypeValidationErrorBeforeNotFoundCheck() throws Exception {
        String body = """
            {"name":""}
            """;
        mvc.perform(put("/api/pettypes/999999").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void updatePetTypeNotFound() throws Exception {
        String body = """
            {"id":999999,"name":"Ghost"}
            """;
        mvc.perform(put("/api/pettypes/999999").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updatePetTypeForbiddenForOwnerAdmin() throws Exception {
        PetType type = newPetType("dog-" + System.nanoTime());
        // Body must be valid, otherwise the assertion depends on whether validation or
        // authorization runs first rather than on the role check under test.
        String body = """
            {"id":%d,"name":"dog I"}
            """.formatted(type.getId());
        mvc.perform(put("/api/pettypes/" + type.getId()).content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void deletePetTypeSuccess() throws Exception {
        PetType type = newPetType("ToDelete-" + System.nanoTime());
        mvc.perform(delete("/api/pettypes/" + type.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void deletePetTypeNotFound() throws Exception {
        mvc.perform(delete("/api/pettypes/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void deletePetTypeForbiddenForOwnerAdmin() throws Exception {
        PetType type = newPetType("dog-" + System.nanoTime());
        mvc.perform(delete("/api/pettypes/" + type.getId()))
            .andExpect(status().isForbidden());
    }
}
