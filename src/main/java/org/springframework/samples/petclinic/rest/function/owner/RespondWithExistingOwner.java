package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Idempotent-replay responder: sends the originally created owner with 200, given its id from the
 * {@code CheckIdempotency} branch.
 */
public class RespondWithExistingOwner {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response) {
        Owner owner = ownerRepository.findById(ownerId);
        response.send(ownerMapper.toOwnerDto(owner));
    }
}
