package org.springframework.samples.petclinic.rest.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PetRestControllerV1Tests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetMapper petMapper;

    @MockitoBean
    private PetRepository petRepository;

    @MockitoBean
    private PetTypeRepository petTypeRepository;

    private List<PetDto> pets;

    @BeforeEach
    void initPets() {
        pets = new ArrayList<>();

        PetTypeDto petType = new PetTypeDto();
        petType.id(2).name("dog");

        PetDto pet = new PetDto();
        pets.add(pet.id(3).name("Rosy").birthDate(LocalDate.now()).type(petType));

        pet = new PetDto();
        pets.add(pet.id(4).name("Jewel").birthDate(LocalDate.now()).type(petType));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetPetSuccess() throws Exception {
        given(petRepository.findById(3)).willReturn(petMapper.toPet(pets.get(0)));
        mockMvc.perform(get("/api/pets/3").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("Rosy"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetPetNotFound() throws Exception {
        mockMvc.perform(get("/api/pets/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetAllPetsSuccess() throws Exception {
        final Collection<Pet> mockPets = petMapper.toPets(this.pets);
        when(petRepository.findAll()).thenReturn(mockPets);
        mockMvc.perform(get("/api/pets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(3))
            .andExpect(jsonPath("$.[0].name").value("Rosy"))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].name").value("Jewel"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetAllPetsNotFound() throws Exception {
        pets.clear();
        given(petRepository.findAll()).willReturn(petMapper.toPets(pets));
        mockMvc.perform(get("/api/pets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testUpdatePetSuccess() throws Exception {
        given(petRepository.findById(3)).willReturn(petMapper.toPet(pets.get(0)));
        PetDto newPet = pets.get(0);
        newPet.setName("Rosy I");
        ObjectMapper mapper = JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("dd/MM/yyyy"))
            .build();
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        mockMvc.perform(put("/api/pets/3")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pets/3")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("Rosy I"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testUpdatePetError() throws Exception {
        PetDto newPet = pets.get(0);
        newPet.setName(null);
        ObjectMapper mapper = JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("dd/MM/yyyy"))
            .build();
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        mockMvc.perform(put("/api/pets/3")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testDeletePetSuccess() throws Exception {
        given(petRepository.findById(3)).willReturn(petMapper.toPet(pets.get(0)));
        PetDto newPet = pets.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        mockMvc.perform(delete("/api/pets/3")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testDeletePetError() throws Exception {
        PetDto newPet = pets.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        mockMvc.perform(delete("/api/pets/999")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());
    }
}
