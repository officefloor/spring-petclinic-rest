package org.springframework.samples.petclinic.rest.function.pet;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTypeRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SavePetTest {

    @Test
    void reResolvesTheManagedPetTypeBeforeSaving() {
        PetType requestedType = new PetType();
        requestedType.setId(2);
        Pet pet = new Pet();
        pet.setType(requestedType);

        PetType managedType = new PetType();
        managedType.setId(2);
        managedType.setName("dog");

        PetRepository petRepository = mock(PetRepository.class);
        PetTypeRepository petTypeRepository = mock(PetTypeRepository.class);
        when(petTypeRepository.findById(2)).thenReturn(managedType);

        new SavePet().service(pet, petRepository, petTypeRepository);

        assertThat(pet.getType()).isSameAs(managedType);
        verify(petRepository).save(pet);
    }
}
