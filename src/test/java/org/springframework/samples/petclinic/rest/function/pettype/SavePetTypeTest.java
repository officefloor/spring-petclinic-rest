package org.springframework.samples.petclinic.rest.function.pettype;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetTypeRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SavePetTypeTest {

    @Test
    void persists() {
        PetType type = new PetType();
        PetTypeRepository repository = mock(PetTypeRepository.class);

        new SavePetType().service(type, repository);

        verify(repository).save(type);
    }
}
