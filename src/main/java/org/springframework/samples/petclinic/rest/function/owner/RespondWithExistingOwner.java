package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Responds to a repeated idempotent create with the originally created owner and 200 (not 201), so a
 * retry is a safe read of the earlier result rather than a new creation.
 */
public class RespondWithExistingOwner {

    public void service(@Val Owner owner, OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response) {
        response.send(ownerMapper.toOwnerDto(owner));
    }
}
