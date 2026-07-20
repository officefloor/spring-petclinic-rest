package org.springframework.samples.petclinic.rest.function.vet;

import java.net.URI;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.VetMapper;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.rest.dto.VetDto;

public class RespondWithVetCreated {

    public void service(@Val Vet vet, VetMapper vetMapper, ObjectResponse<ResponseEntity<VetDto>> response) {
        VetDto dto = vetMapper.toVetDto(vet);
        response.send(ResponseEntity.created(URI.create("/api/vets/" + vet.getId())).body(dto));
    }
}
