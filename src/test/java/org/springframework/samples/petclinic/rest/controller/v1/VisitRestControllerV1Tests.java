package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;
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
 * Integration tests for the /api/visits endpoints, driven end-to-end
 * through the running application against real repositories.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VisitRestControllerV1Tests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetTypeRepository petTypeRepository;

    private Pet newPet() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Franklin-" + System.nanoTime());
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");
        ownerRepository.save(owner);

        PetType type = new PetType();
        type.setName("dog-" + System.nanoTime());
        petTypeRepository.save(type);

        Pet pet = new Pet();
        pet.setName("Rosy");
        pet.setBirthDate(LocalDate.now());
        pet.setType(type);
        owner.addPet(pet);
        petRepository.save(pet);
        return pet;
    }

    private Visit newVisit(Pet pet, String description) {
        Visit visit = new Visit();
        visit.setDate(LocalDate.now());
        visit.setDescription(description);
        visit.setPet(pet);
        visitRepository.save(visit);
        return visit;
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getVisitSuccess() throws Exception {
        Visit visit = newVisit(newPet(), "rabies shot");
        mvc.perform(get("/api/visits/" + visit.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(visit.getId()))
            .andExpect(jsonPath("$.description").value("rabies shot"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getVisitNotFound() throws Exception {
        mvc.perform(get("/api/visits/999999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void getVisitForbiddenForVetAdmin() throws Exception {
        Visit visit = newVisit(newPet(), "rabies shot");
        mvc.perform(get("/api/visits/" + visit.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void listVisitsSuccess() throws Exception {
        Visit visit = newVisit(newPet(), "neutered");
        mvc.perform(get("/api/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + visit.getId() + ")].description").value("neutered"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createVisitSuccess() throws Exception {
        Pet pet = newPet();
        String body = """
            {"date":"2020-01-15","description":"rabies shot","petId":%d}
            """.formatted(pet.getId());
        mvc.perform(post("/api/visits").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/visits/")))
            .andExpect(jsonPath("$.description").value("rabies shot"))
            .andExpect(jsonPath("$.petId").value(pet.getId()));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createVisitValidationError() throws Exception {
        Pet pet = newPet();
        String body = """
            {"date":"2020-01-15","description":"","petId":%d}
            """.formatted(pet.getId());
        mvc.perform(post("/api/visits").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void createVisitForbiddenForVetAdmin() throws Exception {
        Pet pet = newPet();
        String body = """
            {"date":"2020-01-15","description":"rabies shot","petId":%d}
            """.formatted(pet.getId());
        mvc.perform(post("/api/visits").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updateVisitSuccess() throws Exception {
        Visit visit = newVisit(newPet(), "rabies shot");
        String body = """
            {"date":"2020-01-15","description":"neutered"}
            """;

        mvc.perform(put("/api/visits/" + visit.getId()).content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/visits/" + visit.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("neutered"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updateVisitDoesNotChangePet() throws Exception {
        Pet pet = newPet();
        Visit visit = newVisit(pet, "rabies shot");
        String body = """
            {"date":"2020-01-15","description":"neutered"}
            """;

        mvc.perform(put("/api/visits/" + visit.getId()).content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/visits/" + visit.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.petId").value(pet.getId()));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updateVisitValidationErrorBeforeNotFoundCheck() throws Exception {
        String body = """
            {"date":"2020-01-15","description":""}
            """;
        mvc.perform(put("/api/visits/999999").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updateVisitNotFound() throws Exception {
        String body = """
            {"date":"2020-01-15","description":"Ghost visit"}
            """;
        mvc.perform(put("/api/visits/999999").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void deleteVisitSuccess() throws Exception {
        Visit visit = newVisit(newPet(), "ToDelete");
        mvc.perform(delete("/api/visits/" + visit.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void deleteVisitNotFound() throws Exception {
        mvc.perform(delete("/api/visits/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void deleteVisitForbiddenForVetAdmin() throws Exception {
        Visit visit = newVisit(newPet(), "rabies shot");
        mvc.perform(delete("/api/visits/" + visit.getId()))
            .andExpect(status().isForbidden());
    }
}
