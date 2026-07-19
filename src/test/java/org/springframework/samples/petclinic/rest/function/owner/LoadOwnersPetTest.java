package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoadOwnersPetTest {

    @Test
    void publishesThePetWhenItBelongsToTheOwner() throws Exception {
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setId(1);
        owner.addPet(pet);

        MockVar<Pet> loaded = new MockVar<>();
        new LoadOwnersPet().service(owner, 1, loaded);

        assertThat(loaded.get()).isSameAs(pet);
    }

    @Test
    void throwsNotFoundWhenPetBelongsToADifferentOwner() {
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setId(1);
        // pet is not added to owner - simulates a pet id that exists but for another owner

        assertThatThrownBy(() -> new LoadOwnersPet().service(owner, 1, new MockVar<>()))
            .isInstanceOf(NotFoundException.class);
    }
}
