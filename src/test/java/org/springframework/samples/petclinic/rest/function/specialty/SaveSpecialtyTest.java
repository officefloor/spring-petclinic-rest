package org.springframework.samples.petclinic.rest.function.specialty;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SaveSpecialtyTest {

    @Test
    void persists() {
        Specialty specialty = new Specialty();
        SpecialtyRepository repository = mock(SpecialtyRepository.class);

        new SaveSpecialty().service(specialty, repository);

        verify(repository).save(specialty);
    }
}
