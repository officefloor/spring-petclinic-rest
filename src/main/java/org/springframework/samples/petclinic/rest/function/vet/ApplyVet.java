package org.springframework.samples.petclinic.rest.function.vet;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.rest.dto.VetDto;

public class ApplyVet {

    public void service(@Val Vet vet, @Val VetDto request, SpecialtyRepository specialtyRepository) {
        vet.setFirstName(request.getFirstName());
        vet.setLastName(request.getLastName());
        vet.clearSpecialties();
        for (Specialty specialty : SpecialtyResolution.resolveByName(request.getSpecialties(), specialtyRepository)) {
            vet.addSpecialty(specialty);
        }
    }
}
