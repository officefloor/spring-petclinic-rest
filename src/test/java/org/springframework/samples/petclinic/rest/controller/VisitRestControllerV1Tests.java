package org.springframework.samples.petclinic.rest.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VisitRestControllerV1Tests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VisitMapper visitMapper;

    @MockitoBean
    private VisitRepository visitRepository;

    private List<Visit> visits;

    @BeforeEach
    void initVisits() {
        visits = new ArrayList<>();

        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("Eduardo");
        owner.setLastName("Rodriquez");
        owner.setAddress("2693 Commerce St.");
        owner.setCity("McFarland");
        owner.setTelephone("6085558763");

        PetType petType = new PetType();
        petType.setId(2);
        petType.setName("dog");

        Pet pet = new Pet();
        pet.setId(8);
        pet.setName("Rosy");
        pet.setBirthDate(LocalDate.now());
        pet.setOwner(owner);
        pet.setType(petType);

        Visit visit = new Visit();
        visit.setId(2);
        visit.setPet(pet);
        visit.setDate(LocalDate.now());
        visit.setDescription("rabies shot");
        visits.add(visit);

        visit = new Visit();
        visit.setId(3);
        visit.setPet(pet);
        visit.setDate(LocalDate.now());
        visit.setDescription("neutered");
        visits.add(visit);
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetVisitSuccess() throws Exception {
        given(visitRepository.findById(2)).willReturn(visits.get(0));
        mockMvc.perform(get("/api/visits/2").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.description").value("rabies shot"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetVisitNotFound() throws Exception {
        mockMvc.perform(get("/api/visits/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetAllVisitsSuccess() throws Exception {
        given(visitRepository.findAll()).willReturn(visits);
        mockMvc.perform(get("/api/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(2))
            .andExpect(jsonPath("$.[0].description").value("rabies shot"))
            .andExpect(jsonPath("$.[1].id").value(3))
            .andExpect(jsonPath("$.[1].description").value("neutered"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetAllVisitsNotFound() throws Exception {
        visits.clear();
        given(visitRepository.findAll()).willReturn(visits);
        mockMvc.perform(get("/api/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testCreateVisitSuccess() throws Exception {
        Visit newVisit = visits.get(0);
        newVisit.setId(999);
        ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
        System.out.println("newVisitAsJSON " + newVisitAsJSON);
        mockMvc.perform(post("/api/visits")
                .content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testCreateVisitError() throws Exception {
        Visit newVisit = visits.get(0);
        newVisit.setId(null);
        newVisit.setDescription(null);
        ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
        mockMvc.perform(post("/api/visits")
                .content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testUpdateVisitSuccess() throws Exception {
        given(visitRepository.findById(2)).willReturn(visits.get(0));
        Visit newVisit = visits.get(0);
        newVisit.setDescription("rabies shot test");
        ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
        mockMvc.perform(put("/api/visits/2")
                .content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/visits/2")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.description").value("rabies shot test"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testUpdateVisitError() throws Exception {
        Visit newVisit = visits.get(0);
        newVisit.setDescription(null);
        ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
        mockMvc.perform(put("/api/visits/2")
                .content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testDeleteVisitSuccess() throws Exception {
        Visit newVisit = visits.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
        given(visitRepository.findById(2)).willReturn(visits.get(0));
        mockMvc.perform(delete("/api/visits/2")
                .content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testDeleteVisitError() throws Exception {
        Visit newVisit = visits.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
        mockMvc.perform(delete("/api/visits/999")
                .content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());
    }
}
