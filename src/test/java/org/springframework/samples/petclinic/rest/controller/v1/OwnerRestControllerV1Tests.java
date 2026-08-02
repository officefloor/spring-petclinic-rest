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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the /api/owners endpoints (including nested pets/visits), driven end-to-end
 * through the running application against real repositories.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OwnerRestControllerV1Tests {

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

    private String uniqueLastName(String base) {
        StringBuilder suffix = new StringBuilder();
        for (char c : Long.toString(System.nanoTime()).toCharArray()) {
            suffix.append((char) ('a' + (c - '0')));
        }
        return base + suffix;
    }

    private String uniqueTelephone() {
        // Exactly 10 digits (per the Owner telephone constraint), starting with 9 so it never
        // collides with the seeded 608-prefixed numbers.
        String nanos = Long.toString(System.nanoTime());
        String last9 = nanos.length() >= 9 ? nanos.substring(nanos.length() - 9) : nanos;
        return "9" + String.format("%09d", Long.parseLong(last9));
    }

    private PetType dogType() {
        PetType type = new PetType();
        type.setName("dog-" + System.nanoTime());
        petTypeRepository.save(type);
        return type;
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getOwnerSuccess() throws Exception {
        Owner owner = newOwner("Franklin-" + System.nanoTime());
        mvc.perform(get("/api/owners/" + owner.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(owner.getId()))
            .andExpect(jsonPath("$.firstName").value("George"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getOwnerNotFound() throws Exception {
        mvc.perform(get("/api/owners/999999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void listOwnersByLastNameSuccess() throws Exception {
        String uniqueLastName = "Davis-" + System.nanoTime();
        Owner owner = newOwner(uniqueLastName);
        mvc.perform(get("/api/owners?lastName=" + uniqueLastName).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[0].id").value(owner.getId()))
            .andExpect(jsonPath("$.[0].lastName").value(uniqueLastName));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void listOwnersByLastNameNotFound() throws Exception {
        mvc.perform(get("/api/owners?lastName=NoSuchOwnerLastName").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerSuccess() throws Exception {
        String uniqueLastName = uniqueLastName("Franklin");
        String uniqueTelephone = uniqueTelephone();
        String body = """
            {"firstName":"George","lastName":"%s","address":"110 W. Liberty St.","city":"Madison","telephone":"%s"}
            """.formatted(uniqueLastName, uniqueTelephone);
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/owners/")))
            .andExpect(jsonPath("$.firstName").value("George"))
            .andExpect(jsonPath("$.displayName").value(uniqueLastName + ", George"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerConflictWhenIdenticalOwnerExists() throws Exception {
        String uniqueLastName = uniqueLastName("Franklin");
        Owner existing = newOwner(uniqueLastName);
        String body = """
            {"firstName":"%s","lastName":"%s","address":"%s","city":"%s","telephone":"%s"}
            """.formatted(existing.getFirstName(), existing.getLastName(), existing.getAddress(),
                existing.getCity(), existing.getTelephone());
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerValidationError() throws Exception {
        String body = """
            {"lastName":"Franklin","address":"110 W. Liberty St.","city":"Madison","telephone":"6085551023"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("MethodArgumentNotValidException"))
            .andExpect(jsonPath("$.schemaValidationErrors[0].field").exists());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updateOwnerSuccess() throws Exception {
        Owner owner = newOwner("Franklin-" + System.nanoTime());
        String body = """
            {"firstName":"GeorgeI","lastName":"Franklin","address":"110 W. Liberty St.","city":"Madison","telephone":"6085551023"}
            """;
        mvc.perform(put("/api/owners/" + owner.getId()).content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/owners/" + owner.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("GeorgeI"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updateOwnerValidationErrorBeforeNotFoundCheck() throws Exception {
        // Invalid body is a 400 even for a non-existent owner: validation runs before load.
        String body = """
            {"firstName":"","lastName":"Franklin","address":"110 W. Liberty St.","city":"Madison","telephone":"6085551023"}
            """;
        mvc.perform(put("/api/owners/999999").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updateOwnerNotFound() throws Exception {
        String body = """
            {"firstName":"George","lastName":"Franklin","address":"110 W. Liberty St.","city":"Madison","telephone":"6085551023"}
            """;
        mvc.perform(put("/api/owners/999999").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void deleteOwnerSuccess() throws Exception {
        Owner owner = newOwner("ToDelete-" + System.nanoTime());
        mvc.perform(delete("/api/owners/" + owner.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void deleteOwnerNotFound() throws Exception {
        mvc.perform(delete("/api/owners/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createPetSuccess() throws Exception {
        Owner owner = newOwner("PetOwner-" + System.nanoTime());
        PetType type = dogType();
        String body = """
            {"name":"Rosy","birthDate":"2020-01-15","type":{"id":%d,"name":"%s"}}
            """.formatted(type.getId(), type.getName());
        mvc.perform(post("/api/owners/" + owner.getId() + "/pets").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Rosy"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createPetWithUnknownOwnerReturnsNotFound() throws Exception {
        PetType type = dogType();
        String body = """
            {"name":"Rosy","birthDate":"2020-01-15","type":{"id":%d,"name":"%s"}}
            """.formatted(type.getId(), type.getName());
        mvc.perform(post("/api/owners/999999/pets").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createPetWithMissingNameReturnsBadRequest() throws Exception {
        Owner owner = newOwner("PetOwner-" + System.nanoTime());
        PetType type = dogType();
        String body = """
            {"birthDate":"2020-01-15","type":{"id":%d,"name":"%s"}}
            """.formatted(type.getId(), type.getName());
        mvc.perform(post("/api/owners/" + owner.getId() + "/pets").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("MethodArgumentNotValidException"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getOwnersPetSuccess() throws Exception {
        Owner owner = newOwner("PetOwner-" + System.nanoTime());
        PetType type = dogType();
        Pet pet = new Pet();
        pet.setName("Rosy");
        pet.setBirthDate(LocalDate.now());
        pet.setType(type);
        owner.addPet(pet);
        petRepository.save(pet);

        mvc.perform(get("/api/owners/" + owner.getId() + "/pets/" + pet.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Rosy"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getOwnersPetOwnerNotFound() throws Exception {
        mvc.perform(get("/api/owners/999999/pets/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void getOwnersPetNotBelongingToOwnerReturnsNotFound() throws Exception {
        Owner owner1 = newOwner("PetOwnerA-" + System.nanoTime());
        Owner owner2 = newOwner("PetOwnerB-" + System.nanoTime());
        PetType type = dogType();
        Pet pet = new Pet();
        pet.setName("Rosy");
        pet.setBirthDate(LocalDate.now());
        pet.setType(type);
        owner2.addPet(pet);
        petRepository.save(pet);

        // pet belongs to owner2, not owner1
        mvc.perform(get("/api/owners/" + owner1.getId() + "/pets/" + pet.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updateOwnersPetSuccess() throws Exception {
        Owner owner = newOwner("PetOwner-" + System.nanoTime());
        PetType type = dogType();
        Pet pet = new Pet();
        pet.setName("Rosy");
        pet.setBirthDate(LocalDate.now());
        pet.setType(type);
        owner.addPet(pet);
        petRepository.save(pet);

        String body = """
            {"name":"Rex","birthDate":"2020-01-15","type":{"id":%d,"name":"%s"}}
            """.formatted(type.getId(), type.getName());
        mvc.perform(put("/api/owners/" + owner.getId() + "/pets/" + pet.getId()).content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updateOwnersPetOwnerNotFound() throws Exception {
        PetType type = dogType();
        String body = """
            {"name":"Rex","birthDate":"2020-01-15","type":{"id":%d,"name":"%s"}}
            """.formatted(type.getId(), type.getName());
        mvc.perform(put("/api/owners/999999/pets/1").content(body)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void updateOwnersPetPetNotFound() throws Exception {
        Owner owner = newOwner("PetOwner-" + System.nanoTime());
        PetType type = dogType();
        String body = """
            {"name":"Ghost","birthDate":"2020-01-15","type":{"id":%d,"name":"%s"}}
            """.formatted(type.getId(), type.getName());
        mvc.perform(put("/api/owners/" + owner.getId() + "/pets/999999").content(body)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createVisitSuccess() throws Exception {
        Owner owner = newOwner("VisitOwner-" + System.nanoTime());
        PetType type = dogType();
        Pet pet = new Pet();
        pet.setName("Rosy");
        pet.setBirthDate(LocalDate.now());
        pet.setType(type);
        owner.addPet(pet);
        petRepository.save(pet);

        String body = """
            {"date":"2020-01-15","description":"rabies shot"}
            """;
        mvc.perform(post("/api/owners/" + owner.getId() + "/pets/" + pet.getId() + "/visits").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.description").value("rabies shot"));
    }
}
