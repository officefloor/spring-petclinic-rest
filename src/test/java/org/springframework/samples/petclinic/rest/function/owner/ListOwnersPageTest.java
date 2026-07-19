package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.mapper.OwnerMapperImpl;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListOwnersPageTest {

    @Test
    void defaultsPageAndSizeWhenNotProvided() {
        Owner owner = new Owner();
        owner.setId(1);
        owner.setLastName("Franklin");
        PageRequest expected = PageRequest.of(0, 20, Sort.by("id"));
        OwnerRepository repository = mock(OwnerRepository.class);
        when(repository.findAll(eq(expected))).thenReturn(new PageImpl<>(List.of(owner), expected, 1));

        MockObjectResponse<OwnerPageDto> response = new MockObjectResponse<>();
        new ListOwnersPage().service(null, null, null, repository, new OwnerMapperImpl(), response);

        OwnerPageDto page = response.getObject();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getPage()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void filtersByLastNameAndHonoursPageAndSize() {
        Owner owner = new Owner();
        owner.setId(2);
        owner.setLastName("Davis");
        PageRequest expected = PageRequest.of(1, 2, Sort.by("id"));
        OwnerRepository repository = mock(OwnerRepository.class);
        Page<Owner> page = new PageImpl<>(List.of(owner), expected, 3);
        when(repository.findByLastName(eq("Davis"), eq(expected))).thenReturn(page);

        MockObjectResponse<OwnerPageDto> response = new MockObjectResponse<>();
        new ListOwnersPage().service("Davis", 1, 2, repository, new OwnerMapperImpl(), response);

        OwnerPageDto result = response.getObject();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLastName()).isEqualTo("Davis");
        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}
