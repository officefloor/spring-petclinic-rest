package org.springframework.samples.petclinic.rest.function.visit;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;
import org.springframework.web.bind.annotation.PathVariable;

public class LoadVisit {

    public void service(@PathVariable(name = "visitId") Integer visitId,
            VisitRepository visitRepository, Out<Visit> loaded) throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> visitRepository.findById(visitId), "Visit not found: " + visitId));
    }
}
