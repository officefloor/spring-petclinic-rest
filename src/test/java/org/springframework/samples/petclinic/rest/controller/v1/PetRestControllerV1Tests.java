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
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PetRestControllerV1Tests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetTypeRepository petTypeRepository;

    private Pet newPet(String name) {
        Owner owner = new Owner();
        owner.setFirstName("Eduardo");
        owner.setLastName("Rodriquez");
        owner.setAddress("2693 Commerce St.");
        owner.setCity("McFarland");
        owner.setTelephone("6085558763");
        ownerRepository.save(owner);

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
    void getPetSuccess() throws Exception {
        Pet pet = newPet("Rosy");
        mvc.perform(get("/api/pets/" + pet.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(pet.getId()))
            .andExpect(jsonPath("$.name").value("Rosy"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getPetNotFound() throws Exception {
        mvc.perform(get("/api/pets/999999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void listPetsSuccess() throws Exception {
        Pet pet = newPet("Rosy");
        mvc.perform(get("/api/pets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + pet.getId() + ")].name").value("Rosy"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updatePetSuccess() throws Exception {
        Pet pet = newPet("Rosy");
        PetType type = pet.getType();
        String body = """
            {"name":"Rosy I","birthDate":"2020-01-15","type":{"id":%d,"name":"%s"}}
            """.formatted(type.getId(), type.getName());

        mvc.perform(put("/api/pets/" + pet.getId()).content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/pets/" + pet.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Rosy I"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updatePetValidationError() throws Exception {
        Pet pet = newPet("Rosy");
        String body = """
            {"birthDate":"2020-01-15","type":{"id":%d,"name":"%s"}}
            """.formatted(pet.getType().getId(), pet.getType().getName());

        mvc.perform(put("/api/pets/" + pet.getId()).content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updatePetNotFound() throws Exception {
        PetType type = new PetType();
        type.setName("dog-" + System.nanoTime());
        petTypeRepository.save(type);
        String body = """
            {"name":"Ghost","birthDate":"2020-01-15","type":{"id":%d,"name":"%s"}}
            """.formatted(type.getId(), type.getName());

        mvc.perform(put("/api/pets/999999").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void deletePetSuccess() throws Exception {
        Pet pet = newPet("Rosy");
        mvc.perform(delete("/api/pets/" + pet.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void deletePetNotFound() throws Exception {
        mvc.perform(delete("/api/pets/999999"))
            .andExpect(status().isNotFound());
    }
}
