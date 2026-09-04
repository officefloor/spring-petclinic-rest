package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;

/**
 * Replays a previous create: loads the originally created owner (id supplied by
 * {@link CheckIdempotencyKey} as the {@code @Parameter}) and responds 200 with it, so a
 * repeated create with an already-seen {@code Idempotency-Key} returns the original owner
 * instead of creating a duplicate.
 */
public class RespondWithExistingOwner {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response) throws NotFoundException {
        Owner owner = Lookups.findOrNotFound(() -> ownerRepository.findById(ownerId),
                "Owner not found: " + ownerId);
        response.send(ownerMapper.toOwnerDto(owner));
    }
}
