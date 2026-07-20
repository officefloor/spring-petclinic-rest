package org.springframework.samples.petclinic.rest.function.vet;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.VetMapper;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.rest.dto.VetDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildVet {

    public void service(@Valid @RequestBody VetDto request, VetMapper vetMapper,
            SpecialtyRepository specialtyRepository, Out<Vet> built) {
        Vet vet = vetMapper.toVet(request);
        vet.setSpecialties(SpecialtyResolution.resolveByName(request.getSpecialties(), specialtyRepository));
        built.set(vet);
    }
}
