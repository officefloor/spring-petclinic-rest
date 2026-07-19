package org.springframework.samples.petclinic.rest.function.vet;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.VetRepository;

public class SaveVet {

    public void service(@Val Vet vet, VetRepository vetRepository) {
        vetRepository.save(vet);
    }
}
