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
        // Distinct address so this household does not clash with the seeded 'Franklin, 110 W. Liberty St.'.
        String body = """
            {"firstName":"George","lastName":"Franklin","address":"1 Create Success Way","city":"Madison","telephone":"6085550001"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/owners/")))
            .andExpect(jsonPath("$.firstName").value("George"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerNormalizesTelephoneToE164() throws Exception {
        // Distinct address so this household does not clash with the seeded 'Franklin, 110 W. Liberty St.'.
        String body = """
            {"firstName":"George","lastName":"Franklin","address":"2 Normalize Ave","city":"Madison","telephone":"(608) 555-0002"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.telephone").value("+616085550002"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerRejectsTelephoneWithTooFewDigits() throws Exception {
        // Too short to form E.164: assuming '+61' yields '+6112', only 4 digits after the '+'.
        String body = """
            {"firstName":"George","lastName":"Franklin","address":"110 W. Liberty St.","city":"Madison","telephone":"12"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("MethodArgumentNotValidException"))
            .andExpect(jsonPath("$.schemaValidationErrors[0].field").value("telephone"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerRejectsTelephoneWithTooManyDigits() throws Exception {
        // 16 digits after an explicit '+' exceeds the 15-digit E.164 maximum.
        String body = """
            {"firstName":"George","lastName":"Franklin","address":"110 W. Liberty St.","city":"Madison","telephone":"+1234567890123456"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.schemaValidationErrors[0].field").value("telephone"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerRejectsDuplicateNormalizedTelephone() throws Exception {
        Owner existing = newOwner("Franklin-" + System.nanoTime());
        existing.setTelephone("+616085551999");
        ownerRepository.save(existing);

        // Same telephone as the existing owner, just formatted differently: '(608) 555-1999'
        // normalizes to the same E.164 value '+616085551999', so a 409 after normalization.
        String body = """
            {"firstName":"George","lastName":"Franklin","address":"110 W. Liberty St.","city":"Madison","telephone":"(608) 555-1999"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerAllowsSameHouseholdWithDifferentTelephone() throws Exception {
        // Seed data already holds 'Franklin, 110 W. Liberty St.'. This body has the same last name
        // and address (differing only in case and collapsed whitespace) but a fresh telephone. All
        // duplicate detection is now a single identityKey (telephone|email|householdId); since the
        // telephone differs, the whole key differs, so this is no longer a household conflict and is
        // created.
        String body = """
            {"firstName":"Jane","lastName":"Franklin","address":"110   w.  liberty st.","city":"Madison","telephone":"6085552001"}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerAllowsSharedHouseholdWhenAcknowledged() throws Exception {
        // Same household as the seeded 'Franklin, 110 W. Liberty St.', but the request acknowledges
        // the shared household, so it is created.
        String body = """
            {"firstName":"Jane","lastName":"Franklin","address":"110 W. Liberty St.","city":"Madison","telephone":"6085552002","sharesHousehold":true}
            """;
        mvc.perform(post("/api/owners").content(body)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerRejectsHouseholdDuplicate() throws Exception {
        // householdId is now deterministic from (lastName, postcode): a second owner with the same
        // last name and postcode is the same household, so even with a different telephone/address it
        // is rejected as a household duplicate (409) when 'sharesHousehold' is not set.
        String lastName = "Householdtest";
        String first = """
            {"firstName":"George","lastName":"%s","address":"1 First St","city":"Madison","telephone":"6085553001","postcode":"2000"}
            """.formatted(lastName);
        mvc.perform(post("/api/owners").content(first)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        String second = """
            {"firstName":"Jane","lastName":"%s","address":"2 Second St","city":"Madison","telephone":"6085553002","postcode":"2000"}
            """.formatted(lastName);
        mvc.perform(post("/api/owners").content(second)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerAllowsHouseholdDuplicateWhenSharesHousehold() throws Exception {
        // The same household as above, but the second owner acknowledges it with 'sharesHousehold':
        // it is created as a declared household member (with a householdId) and a declared member is
        // not flagged as a possible duplicate.
        String lastName = "Sharedhousehold";
        String first = """
            {"firstName":"George","lastName":"%s","address":"1 First St","city":"Madison","telephone":"6085554001","postcode":"2000"}
            """.formatted(lastName);
        mvc.perform(post("/api/owners").content(first)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        String second = """
            {"firstName":"Jane","lastName":"%s","address":"2 Second St","city":"Madison","telephone":"6085554002","postcode":"2000","sharesHousehold":true}
            """.formatted(lastName);
        mvc.perform(post("/api/owners").content(second)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.householdId").exists())
            .andExpect(jsonPath("$.possibleDuplicate").value(false));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void createOwnerRejectsCityAtCapacity() throws Exception {
        // Fill a fresh city to its 50-owner capacity, each owner with a distinct household and
        // telephone so only the city-capacity rule can reject the 51st create.
        String city = "Capacity-" + System.nanoTime();
        for (int i = 0; i < 50; i++) {
            Owner owner = new Owner();
            owner.setFirstName("Resident" + i);
            owner.setLastName("Resident" + i);
            owner.setAddress(i + " Capacity Ln");
            owner.setCity(city);
            owner.setTelephone("+61608600" + String.format("%04d", i));
            ownerRepository.save(owner);
        }

        String body = """
            {"firstName":"George","lastName":"Overflow","address":"999 Overflow Way","city":"%s","telephone":"6085559999"}
            """.formatted(city);
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
