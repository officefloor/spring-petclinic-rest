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

    /**
     * Builds a unique, letters-only name (the owner name Bean Validation pattern rejects digits),
     * so tests can create distinct owners that do not collide with each other or the seed data.
     */
    private String uniqueName(String prefix) {
        StringBuilder sb = new StringBuilder(prefix);
        for (char c : Long.toString(System.nanoTime()).toCharArray()) {
            sb.append((char) ('a' + (c - '0')));
        }
        return sb.toString();
    }

    /**
     * Builds a unique, exactly-10-digit telephone number (the telephone Bean Validation pattern
     * requires 10 digits) that starts with '7' so it never collides with the '608555...' seed data
     * or with other tests.
     */
    private String uniqueTelephone() {
        return String.format("7%09d", Math.abs(System.nanoTime()) % 1_000_000_000L);
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
        String uniqueLastName = uniqueName("Franklin");
        String uniqueTelephone = uniqueTelephone();
        String body = """
            {"firstName":"George","lastName":"%s","address":"110 W. Liberty St.","city":"Madison","telephone":"%s"}
            """.formatted(uniqueLastName, uniqueTelephone);
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/owners/")))
            .andExpect(jsonPath("$.firstName").value("George"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerAssignsCustomerCode() throws Exception {
        // A city no other owner uses, so the first owner created there is numbered 0001 and
        // the second 0002, both prefixed with the upper-cased city name.
        String city = uniqueName("Metropolis");
        String upperCity = city.toUpperCase(java.util.Locale.ROOT);

        String firstBody = """
            {"firstName":"George","lastName":"%s","address":"110 W. Liberty St.","city":"%s","telephone":"%s"}
            """.formatted(uniqueName("First"), city, uniqueTelephone());
        mvc.perform(post("/api/owners").content(firstBody)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerCode").value(upperCity + "-0001"));

        String secondBody = """
            {"firstName":"George","lastName":"%s","address":"110 W. Liberty St.","city":"%s","telephone":"%s"}
            """.formatted(uniqueName("Second"), city, uniqueTelephone());
        mvc.perform(post("/api/owners").content(secondBody)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerCode").value(upperCity + "-0002"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerTitleCasesCity() throws Exception {
        // A brand-new city (unique suffix) no other owner uses, supplied with mixed casing;
        // it should be stored and returned title-cased.
        String rand = uniqueName("");
        String cityInput = "nORth " + rand;
        String expectedCity = "North " + Character.toUpperCase(rand.charAt(0)) + rand.substring(1);

        String body = """
            {"firstName":"George","lastName":"%s","address":"110 W. Liberty St.","city":"%s","telephone":"%s"}
            """.formatted(uniqueName("Franklin"), cityInput, uniqueTelephone());
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.city").value(expectedCity));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerReusesExistingCitySpelling() throws Exception {
        // An owner already lives in the city with a deliberately odd spelling; a new owner
        // supplying the same city (different casing) should adopt that exact spelling.
        String suffix = Long.toString(Math.abs(System.nanoTime()));
        String existingSpelling = "gOtHaM" + suffix;
        Owner existing = new Owner();
        existing.setFirstName("Bruce");
        existing.setLastName(uniqueName("Wayne"));
        existing.setAddress("1007 Mountain Drive");
        existing.setCity(existingSpelling);
        existing.setTelephone(uniqueTelephone());
        ownerRepository.save(existing);

        String body = """
            {"firstName":"George","lastName":"%s","address":"110 W. Liberty St.","city":"%s","telephone":"%s"}
            """.formatted(uniqueName("Franklin"), "GOTHAM" + suffix, uniqueTelephone());
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.city").value(existingSpelling));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerDuplicateTelephoneConflict() throws Exception {
        // George Franklin from the seed data already uses telephone 6085551023, so a new
        // owner reusing that number must be rejected even though every other field differs.
        String body = """
            {"firstName":"Georgina","lastName":"%s","address":"1 Elsewhere Rd.","city":"Verona","telephone":"6085551023"}
            """.formatted(uniqueName("Elsewhere"));
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerWithEmailStoresAndReturnsEmail() throws Exception {
        String uniqueLastName = uniqueName("Emailer");
        String uniqueTelephone = uniqueTelephone();
        String email = "george." + System.nanoTime() + "@example.com";
        String body = """
            {"firstName":"George","lastName":"%s","address":"110 W. Liberty St.","city":"Madison","telephone":"%s","email":"%s"}
            """.formatted(uniqueLastName, uniqueTelephone, email);
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerDuplicateEmailConflict() throws Exception {
        String email = "shared." + System.nanoTime() + "@example.com";
        String firstBody = """
            {"firstName":"George","lastName":"%s","address":"110 W. Liberty St.","city":"Madison","telephone":"%s","email":"%s"}
            """.formatted(uniqueName("First"), uniqueTelephone(), email);
        mvc.perform(post("/api/owners").content(firstBody)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        // A second owner reusing the same email must be rejected even though every other field differs.
        String secondBody = """
            {"firstName":"Georgina","lastName":"%s","address":"1 Elsewhere Rd.","city":"Verona","telephone":"%s","email":"%s"}
            """.formatted(uniqueName("Second"), uniqueTelephone(), email);
        mvc.perform(post("/api/owners").content(secondBody)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerWithoutEmailSucceeds() throws Exception {
        String body = """
            {"firstName":"George","lastName":"%s","address":"110 W. Liberty St.","city":"Madison","telephone":"%s"}
            """.formatted(uniqueName("NoEmail"), uniqueTelephone());
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerDuplicateConflict() throws Exception {
        String uniqueLastName = uniqueName("Franklin");
        newOwner(uniqueLastName);
        String body = """
            {"firstName":"George","lastName":"%s","address":"110 W. Liberty St.","city":"Madison","telephone":"6085551023"}
            """.formatted(uniqueLastName);
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
