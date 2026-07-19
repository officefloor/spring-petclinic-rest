package org.springframework.samples.petclinic.rest.controller.v2;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the paginated /api/v2/owners and /api/v2/pets endpoints, driven against
 * the OfficeFloor REST YAML pipelines rather than Spring MVC controllers.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class V2RestControllersTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetTypeRepository petTypeRepository;

    private Owner newOwner(String lastName) {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName(lastName);
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");
        ownerRepository.save(owner);
        return owner;
    }

    private Pet newPet(String name) {
        Owner owner = newOwner("Franklin-" + System.nanoTime());
        PetType type = new PetType();
        type.setName("dog-" + System.nanoTime());
        petTypeRepository.save(type);

        Pet pet = new Pet();
        pet.setName(name);
        pet.setBirthDate(LocalDate.now());
        pet.setType(type);
        owner.addPet(pet);
        petRepository.save(pet);
        return pet;
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void listOwnersPageDefaultsToPageZeroSizeTwenty() throws Exception {
        Owner owner = newOwner("Franklin-" + System.nanoTime());
        mvc.perform(get("/api/v2/owners").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.content[?(@.id == " + owner.getId() + ")]").exists());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void listOwnersPageFiltersByLastName() throws Exception {
        String uniqueLastName = "Davis-" + System.nanoTime();
        Owner owner = newOwner(uniqueLastName);
        mvc.perform(get("/api/v2/owners?lastName=" + uniqueLastName).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(owner.getId()))
            .andExpect(jsonPath("$.content[0].lastName").value(uniqueLastName))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void listOwnersPageHonoursPageAndSize() throws Exception {
        mvc.perform(get("/api/v2/owners?page=0&size=2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(2)));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void listOwnersPageForbiddenForVetAdmin() throws Exception {
        mvc.perform(get("/api/v2/owners").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void listPetsPageDefaultsToPageZeroSizeTwenty() throws Exception {
        Pet pet = newPet("Rosy");
        mvc.perform(get("/api/v2/pets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.content[?(@.id == " + pet.getId() + ")]").exists());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void listPetsPageHonoursPageAndSize() throws Exception {
        newPet("Jewel");
        mvc.perform(get("/api/v2/pets?page=0&size=1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "VET_ADMIN")
    void listPetsPageForbiddenForVetAdmin() throws Exception {
        mvc.perform(get("/api/v2/pets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }
}
