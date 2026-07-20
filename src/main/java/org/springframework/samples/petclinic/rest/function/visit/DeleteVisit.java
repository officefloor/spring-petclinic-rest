package org.springframework.samples.petclinic.rest.function.visit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.VisitRepository;

public class DeleteVisit {

    public void service(@Val Visit visit, VisitRepository visitRepository) {
        visitRepository.delete(visit);
    }
}
