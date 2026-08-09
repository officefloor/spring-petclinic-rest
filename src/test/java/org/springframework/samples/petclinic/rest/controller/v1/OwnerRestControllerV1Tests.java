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
        // Distinct address from the seeded George Franklin so it is not rejected as a household duplicate.
        String body = """
            {"firstName":"George","lastName":"Franklin","address":"200 E. Washington Ave.","city":"Madison","telephone":"6085550000"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/owners/")))
            .andExpect(jsonPath("$.firstName").value("George"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerSameHouseholdDifferentTelephoneAllowed() throws Exception {
        String first = """
            {"firstName":"George","lastName":"Householder","address":"1 Shared Lane","city":"Madison","telephone":"6085550101"}
            """;
        mvc.perform(post("/api/owners").content(first)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        // Same last name and address (differing only in case and whitespace) but a DIFFERENT
        // telephone: the telephone is part of the identityKey, so the whole keys differ -> created.
        String sameHousehold = """
            {"firstName":"Jane","lastName":"householder","address":"1   Shared   Lane","city":"Madison","telephone":"6085550102"}
            """;
        mvc.perform(post("/api/owners").content(sameHousehold)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerIdentityKeyCollisionReturnsConflict() throws Exception {
        // A fully unique owner is created and returns its derived identityKey.
        String first = """
            {"firstName":"George","lastName":"Collider","address":"5 Identity Way","city":"Madison","telephone":"6085550120"}
            """;
        mvc.perform(post("/api/owners").content(first)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.identityKey").value("+16085550120||"));

        // Different name/address, but the SAME telephone, no email and no household: the whole
        // identityKey matches -> 409.
        String duplicate = """
            {"firstName":"Jane","lastName":"Twin","address":"9 Other Road","city":"Madison","telephone":"6085550120"}
            """;
        mvc.perform(post("/api/owners").content(duplicate)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerHouseholdDuplicateAllowedWhenSharesHousehold() throws Exception {
        String first = """
            {"firstName":"George","lastName":"Cohabitant","address":"9 Oak Street","city":"Madison","telephone":"6085550201"}
            """;
        mvc.perform(post("/api/owners").content(first)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        // Same household, but sharesHousehold=true opts in -> created.
        String sameHousehold = """
            {"firstName":"Jane","lastName":"Cohabitant","address":"9 Oak Street","city":"Madison","telephone":"6085550202","sharesHousehold":true}
            """;
        mvc.perform(post("/api/owners").content(sameHousehold)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerSameEmailDifferentTelephoneAllowed() throws Exception {
        String first = """
            {"firstName":"George","lastName":"Mailer","address":"3 First Street","city":"Madison","telephone":"6085550301","email":"George.Mailer@Example.com"}
            """;
        mvc.perform(post("/api/owners").content(first)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        // Same email (differing only in case) but a DIFFERENT telephone: the telephone is part of
        // the identityKey, so the whole keys differ -> created.
        String sameEmail = """
            {"firstName":"Jane","lastName":"Poster","address":"7 Second Street","city":"Madison","telephone":"6085550302","email":"george.mailer@example.com"}
            """;
        mvc.perform(post("/api/owners").content(sameEmail)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerRejectedWhenCityAtCapacity() throws Exception {
        // A city that already holds 50 owners is full: the next creation is a 409.
        String city = "Capville-" + System.nanoTime();
        for (int i = 0; i < 50; i++) {
            Owner owner = new Owner();
            owner.setFirstName("Resident" + i);
            owner.setLastName("Full-" + System.nanoTime() + "-" + i);
            owner.setAddress(i + " Capacity Street");
            owner.setCity(city);
            owner.setTelephone("60855" + String.format("%05d", i));
            ownerRepository.save(owner);
        }

        String body = """
            {"firstName":"George","lastName":"Overflow","address":"999 Capacity Street","city":"%s","telephone":"6085559999"}
            """.formatted(city);
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerAllowedWhenCityBelowCapacity() throws Exception {
        // A city with 49 owners still has room: the next creation succeeds.
        String city = "Roomyville-" + System.nanoTime();
        for (int i = 0; i < 49; i++) {
            Owner owner = new Owner();
            owner.setFirstName("Resident" + i);
            owner.setLastName("Room-" + System.nanoTime() + "-" + i);
            owner.setAddress(i + " Roomy Street");
            owner.setCity(city);
            owner.setTelephone("60856" + String.format("%05d", i));
            ownerRepository.save(owner);
        }

        String body = """
            {"firstName":"George","lastName":"Newcomer","address":"999 Roomy Street","city":"%s","telephone":"6085558888"}
            """.formatted(city);
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    private void seedOwnersRegisteredToday(int count) {
        for (int i = 0; i < count; i++) {
            long tag = System.nanoTime();
            Owner owner = new Owner();
            owner.setFirstName("Daily" + i);
            owner.setLastName("Today-" + tag + "-" + i);
            owner.setAddress(i + " Today Street " + tag);
            owner.setCity("Dayville-" + tag + "-" + i);
            owner.setTelephone("60857" + String.format("%05d", i));
            owner.setRegistrationDate(LocalDate.now());
            ownerRepository.save(owner);
        }
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerRejectedWhenDailyLimitReached() throws Exception {
        // Once 100 owners already carry today's registration date, the next creation is a 429.
        seedOwnersRegisteredToday(100);

        String body = """
            {"firstName":"George","lastName":"Latecomer","address":"999 Daily Street","city":"Freshville","telephone":"6085557777"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isTooManyRequests());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerAllowedWhenBelowDailyLimit() throws Exception {
        // With only 99 owners registered today there is still room: the next creation succeeds.
        seedOwnersRegisteredToday(99);

        String body = """
            {"firstName":"George","lastName":"Earlybird","address":"1000 Daily Street","city":"Freshville","telephone":"6085556666"}
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
    void createOwnerMissingAndBlankFieldsListedInErrors() throws Exception {
        // firstName missing, address blank (whitespace), city empty -> all three must be reported.
        String body = """
            {"lastName":"Franklin","address":"   ","city":"","telephone":"6085551023"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", org.hamcrest.Matchers.containsInAnyOrder("firstName", "address", "city")));
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
