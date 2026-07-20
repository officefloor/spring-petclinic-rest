package org.springframework.samples.petclinic.rest.function.visit;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoadVisitTest {

    @Test
    void publishesTheVisit() throws Exception {
        Visit visit = new Visit();
        visit.setId(2);
        VisitRepository repository = mock(VisitRepository.class);
        when(repository.findById(2)).thenReturn(visit);

        MockVar<Visit> loaded = new MockVar<>();
        new LoadVisit().service(2, repository, loaded);

        assertThat(loaded.get()).isSameAs(visit);
    }

    @Test
    void throwsNotFoundWhenMissing() {
        VisitRepository repository = mock(VisitRepository.class);
        when(repository.findById(99)).thenThrow(new ObjectRetrievalFailureException(Visit.class, 99));

        assertThatThrownBy(() -> new LoadVisit().service(99, repository, new MockVar<>()))
            .isInstanceOf(NotFoundException.class);
    }
}
