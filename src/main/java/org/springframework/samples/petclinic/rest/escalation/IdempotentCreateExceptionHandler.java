package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Responds 200 with the originally created owner for a repeated idempotent create.
 */
public class IdempotentCreateExceptionHandler {

    public void handle(@Parameter IdempotentCreateException ex, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response) {
        Owner owner = ownerRepository.findById(ex.getOwnerId());
        response.send(ownerMapper.toOwnerDto(owner));
    }
}
