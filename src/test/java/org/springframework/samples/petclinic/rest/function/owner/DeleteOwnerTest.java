package org.springframework.samples.petclinic.rest.function.owner;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DeleteOwnerTest {

    @Test
    void deletes() {
        Owner owner = new Owner();
        OwnerRepository repository = mock(OwnerRepository.class);

        new DeleteOwner().service(owner, repository);

        verify(repository).delete(owner);
    }
}
