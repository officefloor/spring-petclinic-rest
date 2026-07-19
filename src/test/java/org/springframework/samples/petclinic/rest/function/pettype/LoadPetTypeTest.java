package org.springframework.samples.petclinic.rest.function.pettype;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoadPetTypeTest {

    @Test
    void publishesThePetType() throws Exception {
        PetType type = new PetType();
        type.setId(2);
        PetTypeRepository repository = mock(PetTypeRepository.class);
        when(repository.findById(2)).thenReturn(type);

        MockVar<PetType> loaded = new MockVar<>();
        new LoadPetType().service(2, repository, loaded);

        assertThat(loaded.get()).isSameAs(type);
    }

    @Test
    void throwsNotFoundWhenMissing() {
        PetTypeRepository repository = mock(PetTypeRepository.class);
        when(repository.findById(99)).thenThrow(new ObjectRetrievalFailureException(PetType.class, 99));

        assertThatThrownBy(() -> new LoadPetType().service(99, repository, new MockVar<>()))
            .isInstanceOf(NotFoundException.class);
    }
}
