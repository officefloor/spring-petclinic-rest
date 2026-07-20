package org.springframework.samples.petclinic.rest.function.vet;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoadVetTest {

    @Test
    void publishesTheVet() throws Exception {
        Vet vet = new Vet();
        vet.setId(2);
        VetRepository repository = mock(VetRepository.class);
        when(repository.findById(2)).thenReturn(vet);

        MockVar<Vet> loaded = new MockVar<>();
        new LoadVet().service(2, repository, loaded);

        assertThat(loaded.get()).isSameAs(vet);
    }

    @Test
    void throwsNotFoundWhenMissing() {
        VetRepository repository = mock(VetRepository.class);
        when(repository.findById(99)).thenThrow(new ObjectRetrievalFailureException(Vet.class, 99));

        assertThatThrownBy(() -> new LoadVet().service(99, repository, new MockVar<>()))
            .isInstanceOf(NotFoundException.class);
    }
}
