package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Replays the owner an {@code Idempotency-Key} first created: loads it by the id
 * {@link CheckIdempotency} resolved from the key and responds 200, as opposed to
 * {@link RespondWithOwnerCreated}'s 201 for a fresh create.
 */
public class RespondWithExistingOwner {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response) {
        Owner owner = ownerRepository.findById(ownerId);
        response.send(ownerMapper.toOwnerDto(owner));
    }
}
