package org.springframework.samples.petclinic.rest.function.specialty;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoadSpecialtyTest {

    @Test
    void publishesTheSpecialty() throws Exception {
        Specialty specialty = new Specialty();
        specialty.setId(2);
        SpecialtyRepository repository = mock(SpecialtyRepository.class);
        when(repository.findById(2)).thenReturn(specialty);

        MockVar<Specialty> loaded = new MockVar<>();
        new LoadSpecialty().service(2, repository, loaded);

        assertThat(loaded.get()).isSameAs(specialty);
    }

    @Test
    void throwsNotFoundWhenMissing() {
        SpecialtyRepository repository = mock(SpecialtyRepository.class);
        when(repository.findById(99)).thenThrow(new ObjectRetrievalFailureException(Specialty.class, 99));

        assertThatThrownBy(() -> new LoadSpecialty().service(99, repository, new MockVar<>()))
            .isInstanceOf(NotFoundException.class);
    }
}
