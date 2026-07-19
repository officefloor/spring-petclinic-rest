package org.springframework.samples.petclinic.rest.function.pet;

import java.util.List;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.mapper.PetMapperImpl;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.rest.dto.PetPageDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListPetsPageTest {

    @Test
    void defaultsPageAndSizeWhenNotProvided() {
        Pet pet = new Pet();
        pet.setId(3);
        pet.setName("Rosy");
        PageRequest expected = PageRequest.of(0, 20, Sort.by("id"));
        PetRepository repository = mock(PetRepository.class);
        when(repository.findAll(eq(expected))).thenReturn(new PageImpl<>(List.of(pet), expected, 1));

        MockObjectResponse<PetPageDto> response = new MockObjectResponse<>();
        new ListPetsPage().service(null, null, repository, new PetMapperImpl(), response);

        PetPageDto page = response.getObject();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getPage()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(20);
    }

    @Test
    void honoursPageAndSize() {
        Pet pet = new Pet();
        pet.setId(4);
        pet.setName("Jewel");
        // total must stay consistent with offset + content size, or PageImpl recalculates it
        // (see its 3-arg constructor: total is overridden when offset + pageSize > total).
        PageRequest expected = PageRequest.of(1, 5, Sort.by("id"));
        PetRepository repository = mock(PetRepository.class);
        when(repository.findAll(eq(expected))).thenReturn(new PageImpl<>(List.of(pet), expected, 6));

        MockObjectResponse<PetPageDto> response = new MockObjectResponse<>();
        new ListPetsPage().service(1, 5, repository, new PetMapperImpl(), response);

        PetPageDto result = response.getObject();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Jewel");
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(6);
    }
}
