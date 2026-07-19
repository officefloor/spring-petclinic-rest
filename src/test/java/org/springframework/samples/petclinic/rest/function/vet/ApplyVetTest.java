package org.springframework.samples.petclinic.rest.function.vet;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;
import org.springframework.samples.petclinic.rest.dto.VetDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplyVetTest {

    @Test
    void replacesNameAndResolvesNewSpecialties() {
        Vet vet = new Vet();
        vet.setId(2);
        vet.setFirstName("old");
        vet.setLastName("name");
        Specialty stale = new Specialty();
        stale.setId(1);
        stale.setName("stale");
        vet.addSpecialty(stale);

        VetDto request = new VetDto().firstName("James").lastName("Carter")
            .specialties(List.of(new SpecialtyDto().name("dentistry")));

        Specialty managed = new Specialty();
        managed.setId(6);
        managed.setName("dentistry");
        SpecialtyRepository specialtyRepository = mock(SpecialtyRepository.class);
        when(specialtyRepository.findSpecialtiesByNameIn(any())).thenReturn(List.of(managed));

        new ApplyVet().service(vet, request, specialtyRepository);

        assertThat(vet.getFirstName()).isEqualTo("James");
        assertThat(vet.getLastName()).isEqualTo("Carter");
        assertThat(vet.getSpecialties()).containsExactly(managed);
    }

    @Test
    void clearsSpecialtiesWhenNoneRequested() {
        Vet vet = new Vet();
        Specialty stale = new Specialty();
        stale.setId(1);
        stale.setName("stale");
        vet.addSpecialty(stale);

        VetDto request = new VetDto().firstName("James").lastName("Carter").specialties(List.of());
        SpecialtyRepository specialtyRepository = mock(SpecialtyRepository.class);

        new ApplyVet().service(vet, request, specialtyRepository);

        assertThat(vet.getSpecialties()).isEmpty();
    }
}
