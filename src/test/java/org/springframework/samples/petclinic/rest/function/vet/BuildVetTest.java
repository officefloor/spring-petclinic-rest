package org.springframework.samples.petclinic.rest.function.vet;

import java.util.List;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.VetMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;
import org.springframework.samples.petclinic.rest.dto.VetDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BuildVetTest {

    @Test
    void resolvesNamedSpecialtiesToManagedEntities() {
        VetDto request = new VetDto().firstName("James").lastName("Carter")
            .specialties(List.of(new SpecialtyDto().name("radiology")));

        Vet mapped = new Vet();
        mapped.setFirstName("James");
        mapped.setLastName("Carter");
        VetMapper vetMapper = mock(VetMapper.class);
        when(vetMapper.toVet(request)).thenReturn(mapped);

        Specialty managed = new Specialty();
        managed.setId(5);
        managed.setName("radiology");
        SpecialtyRepository specialtyRepository = mock(SpecialtyRepository.class);
        when(specialtyRepository.findSpecialtiesByNameIn(any())).thenReturn(List.of(managed));

        MockVar<Vet> built = new MockVar<>();
        new BuildVet().service(request, vetMapper, specialtyRepository, built);

        Vet vet = built.get();
        assertThat(vet.getFirstName()).isEqualTo("James");
        assertThat(vet.getSpecialties()).containsExactly(managed);
    }

    @Test
    void skipsResolutionWhenNoSpecialties() {
        VetDto request = new VetDto().firstName("James").lastName("Carter").specialties(List.of());
        Vet mapped = new Vet();
        mapped.setFirstName("James");
        mapped.setLastName("Carter");
        VetMapper vetMapper = mock(VetMapper.class);
        when(vetMapper.toVet(request)).thenReturn(mapped);
        SpecialtyRepository specialtyRepository = mock(SpecialtyRepository.class);

        MockVar<Vet> built = new MockVar<>();
        new BuildVet().service(request, vetMapper, specialtyRepository, built);

        assertThat(built.get().getSpecialties()).isEmpty();
        verifyNoInteractions(specialtyRepository);
    }
}
