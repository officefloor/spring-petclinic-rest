package org.springframework.samples.petclinic.rest.function.visit;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.VisitRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SaveVisitTest {

    @Test
    void persists() {
        Visit visit = new Visit();
        VisitRepository repository = mock(VisitRepository.class);

        new SaveVisit().service(visit, repository);

        verify(repository).save(visit);
    }
}
