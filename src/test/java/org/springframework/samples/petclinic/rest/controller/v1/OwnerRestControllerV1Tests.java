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
        // Telephone not used by any existing owner, so creation is allowed.
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"Madison","telephone":"6085550000"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/owners/")))
            .andExpect(jsonPath("$.firstName").value("George"))
            .andExpect(jsonPath("$.displayName").value("Washington, George"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerWithEmailSuccess() throws Exception {
        // Unique telephone and email, so creation is allowed and the email is echoed back.
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"Madison","telephone":"6085550001","email":"george.washington@example.com"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("george.washington@example.com"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerAssignsCustomerCodeForNewCity() throws Exception {
        // No seed owners live in London, so the first owner there is numbered 0001.
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"London","telephone":"6085550010"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerCode").value("LONDON-0001"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerAssignsCustomerCodeBasedOnCityCount() throws Exception {
        // Four seed owners already live in Madison, so the next Madison owner is numbered 0005.
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"Madison","telephone":"6085550011"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerCode").value("MADISON-0005"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerTitleCasesCityForNewCity() throws Exception {
        // No owner lives in Springfield yet, so the supplied city is title-cased.
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"sPRINGfield","telephone":"6085550020"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.city").value("Springfield"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerTitleCasesMultiWordCity() throws Exception {
        // A multi-word city not yet used has each word title-cased.
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"new york","telephone":"6085550021"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.city").value("New York"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerReusesExistingCitySpelling() throws Exception {
        // Seed owners live in "Madison"; a differently-cased spelling reuses the existing one.
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"MADISON","telephone":"6085550022"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.city").value("Madison"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerDuplicateEmailConflict() throws Exception {
        String first = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"Madison","telephone":"6085550002","email":"shared.owner@example.com"}
            """;
        mvc.perform(post("/api/owners").content(first)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        // Different telephone but same email as the owner created above must be rejected.
        String second = """
            {"firstName":"Thomas","lastName":"Jefferson","address":"1 Elsewhere Rd.","city":"Boston","telephone":"6085550003","email":"shared.owner@example.com"}
            """;
        mvc.perform(post("/api/owners").content(second)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerDuplicateConflict() throws Exception {
        // George Franklin already exists in the seed data; an identical owner must be rejected.
        String body = """
            {"firstName":"George","lastName":"Franklin","address":"110 W. Liberty St.","city":"Madison","telephone":"6085551023"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerSameLastNameAndTelephoneConflict() throws Exception {
        // Matches George Franklin (seed) on last name and telephone only; other fields differ.
        String body = """
            {"firstName":"Benjamin","lastName":"Franklin","address":"1 Elsewhere Rd.","city":"Boston","telephone":"6085551023"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerDifferentLastNameSameTelephoneConflict() throws Exception {
        // Telephone already used by George Franklin (seed); a different owner reusing it must be rejected.
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"Madison","telephone":"6085551023"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerSameLastNameDifferentTelephoneSuccess() throws Exception {
        // Same last name as the seed owner but a different telephone must be allowed.
        String body = """
            {"firstName":"Benjamin","lastName":"Franklin","address":"1 Elsewhere Rd.","city":"Boston","telephone":"6085559999"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
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

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerRejectedWhenDailyLimitReached() throws Exception {
        // 20 owners already registered today: the 21st creation must be rejected with 400.
        // Each is registered in a distinct city so this exercises only the daily-registration
        // limit and not the per-city owner cap.
        for (int i = 0; i < 20; i++) {
            Owner owner = new Owner();
            owner.setFirstName("Daily");
            owner.setLastName("Limit-" + System.nanoTime());
            owner.setAddress("110 W. Liberty St.");
            owner.setCity("DailyCity-" + i);
            owner.setTelephone("6085551023");
            owner.setRegistrationDate(LocalDate.now());
            ownerRepository.save(owner);
        }
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"Madison","telephone":"6085550000"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerAllowedBelowDailyLimit() throws Exception {
        // 19 owners registered today: another creation is still allowed.
        // Each is registered in a distinct city so this exercises only the daily-registration
        // limit and not the per-city owner cap.
        for (int i = 0; i < 19; i++) {
            Owner owner = new Owner();
            owner.setFirstName("Daily");
            owner.setLastName("Limit-" + System.nanoTime());
            owner.setAddress("110 W. Liberty St.");
            owner.setCity("DailyCity-" + i);
            owner.setTelephone("6085551023");
            owner.setRegistrationDate(LocalDate.now());
            ownerRepository.save(owner);
        }
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"Madison","telephone":"6085550000"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerRejectedWhenCityFull() throws Exception {
        // A city that already contains 8 owners cannot accept any more: the 9th is rejected with 400.
        for (int i = 0; i < 8; i++) {
            Owner owner = new Owner();
            owner.setFirstName("City");
            owner.setLastName("Cap-" + System.nanoTime());
            owner.setAddress("110 W. Liberty St.");
            owner.setCity("Springfield");
            owner.setTelephone("6085551023");
            ownerRepository.save(owner);
        }
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"Springfield","telephone":"6085550000"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerAllowedBelowCityLimit() throws Exception {
        // A city with only 7 owners can still accept the 8th.
        for (int i = 0; i < 7; i++) {
            Owner owner = new Owner();
            owner.setFirstName("City");
            owner.setLastName("Cap-" + System.nanoTime());
            owner.setAddress("110 W. Liberty St.");
            owner.setCity("Springfield");
            owner.setTelephone("6085551023");
            ownerRepository.save(owner);
        }
        String body = """
            {"firstName":"George","lastName":"Washington","address":"110 W. Liberty St.","city":"Springfield","telephone":"6085550000"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }
}
