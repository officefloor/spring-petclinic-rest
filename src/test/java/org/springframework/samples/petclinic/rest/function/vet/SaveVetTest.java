package org.springframework.samples.petclinic.rest.function.vet;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.VetRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SaveVetTest {

    @Test
    void persists() {
        Vet vet = new Vet();
        VetRepository repository = mock(VetRepository.class);

        new SaveVet().service(vet, repository);

        verify(repository).save(vet);
    }
}
