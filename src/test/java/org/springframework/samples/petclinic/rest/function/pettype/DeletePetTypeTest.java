package org.springframework.samples.petclinic.rest.function.pettype;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetTypeRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DeletePetTypeTest {

    @Test
    void deletesTheEntityViaTheRepository() {
        PetType type = new PetType();
        type.setId(2);
        PetTypeRepository repository = mock(PetTypeRepository.class);

        new DeletePetType().service(type, repository);

        verify(repository).delete(type);
    }
}
