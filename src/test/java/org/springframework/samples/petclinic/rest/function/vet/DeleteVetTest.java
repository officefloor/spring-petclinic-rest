package org.springframework.samples.petclinic.rest.function.vet;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.VetRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DeleteVetTest {

    @Test
    void deletesTheEntityViaTheRepository() {
        Vet vet = new Vet();
        vet.setId(2);
        VetRepository repository = mock(VetRepository.class);

        new DeleteVet().service(vet, repository);

        verify(repository).delete(vet);
    }
}
