package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoadOwnerTest {

    @Test
    void publishesTheOwner() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        OwnerRepository repository = mock(OwnerRepository.class);
        when(repository.findById(1)).thenReturn(owner);

        MockVar<Owner> loaded = new MockVar<>();
        new LoadOwner().service(1, repository, loaded);

        assertThat(loaded.get()).isSameAs(owner);
    }

    @Test
    void throwsNotFoundWhenMissing() {
        OwnerRepository repository = mock(OwnerRepository.class);
        when(repository.findById(99)).thenThrow(new ObjectRetrievalFailureException(Owner.class, 99));

        assertThatThrownBy(() -> new LoadOwner().service(99, repository, new MockVar<>()))
            .isInstanceOf(NotFoundException.class);
    }
}
