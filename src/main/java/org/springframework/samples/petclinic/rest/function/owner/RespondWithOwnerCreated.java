package org.springframework.samples.petclinic.rest.function.owner;

import java.net.URI;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwnerCreated {

    public void service(@Val Owner owner, @Val OwnerDto dto,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        response.send(ResponseEntity.created(URI.create("/api/owners/" + owner.getId())).body(dto));
    }
}
