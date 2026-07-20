package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoadPetTest {

    @Test
    void publishesThePet() throws Exception {
        Pet pet = new Pet();
        PetRepository repository = mock(PetRepository.class);
        when(repository.findById(1)).thenReturn(pet);

        MockVar<Pet> loaded = new MockVar<>();
        new LoadPet().service(1, repository, loaded);

        assertThat(loaded.get()).isSameAs(pet);
    }

    @Test
    void throwsNotFoundWhenMissing() {
        PetRepository repository = mock(PetRepository.class);
        when(repository.findById(99)).thenThrow(new ObjectRetrievalFailureException(Pet.class, 99));

        assertThatThrownBy(() -> new LoadPet().service(99, repository, new MockVar<>()))
            .isInstanceOf(NotFoundException.class);
    }
}
