package org.springframework.samples.petclinic.rest.function.visit;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.VisitRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DeleteVisitTest {

    @Test
    void deletesTheEntityViaTheRepository() {
        Visit visit = new Visit();
        visit.setId(2);
        VisitRepository repository = mock(VisitRepository.class);

        new DeleteVisit().service(visit, repository);

        verify(repository).delete(visit);
    }
}
