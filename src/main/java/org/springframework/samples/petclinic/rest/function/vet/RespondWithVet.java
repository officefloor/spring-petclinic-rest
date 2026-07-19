package org.springframework.samples.petclinic.rest.function.vet;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.VetMapper;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.rest.dto.VetDto;

public class RespondWithVet {

    public void service(@Val Vet vet, VetMapper vetMapper, ObjectResponse<VetDto> response) {
        response.send(vetMapper.toVetDto(vet));
    }
}
