package org.springframework.samples.petclinic.rest.function.specialty;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DeleteSpecialtyTest {

    @Test
    void deletesTheEntityViaTheRepository() {
        Specialty specialty = new Specialty();
        specialty.setId(2);
        SpecialtyRepository repository = mock(SpecialtyRepository.class);

        new DeleteSpecialty().service(specialty, repository);

        verify(repository).delete(specialty);
    }
}
