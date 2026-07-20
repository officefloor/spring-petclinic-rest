package org.springframework.samples.petclinic.rest.function.pet;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.repository.PetRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DeletePetTest {

    @Test
    void deletesTheEntityViaTheRepository() {
        Pet pet = new Pet();
        pet.setId(3);
        PetRepository repository = mock(PetRepository.class);

        new DeletePet().service(pet, repository);

        verify(repository).delete(pet);
    }
}
