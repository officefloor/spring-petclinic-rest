package org.springframework.samples.petclinic.rest.function.owner;

import java.net.URI;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwnerCreated {

    public void service(@Val Owner owner, OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        // The self link is the created resource's URL, so reuse it as the Location header
        // rather than rebuilding the path here.
        response.send(ResponseEntity.created(URI.create(dto.getSelfLink())).body(dto));
    }
}
