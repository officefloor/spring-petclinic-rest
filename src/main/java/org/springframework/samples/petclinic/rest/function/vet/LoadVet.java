package org.springframework.samples.petclinic.rest.function.vet;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;
import org.springframework.web.bind.annotation.PathVariable;

public class LoadVet {

    public void service(@PathVariable(name = "vetId") Integer vetId,
            VetRepository vetRepository, Out<Vet> loaded) throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> vetRepository.findById(vetId), "Vet not found: " + vetId));
    }
}
