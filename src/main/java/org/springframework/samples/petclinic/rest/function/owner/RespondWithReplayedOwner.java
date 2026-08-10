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
 * Replays a previously created owner for a repeated {@code Idempotency-Key}: loads the originally
 * created owner (by the id resolved in {@link CheckIdempotencyKey}) and responds 200 with its DTO,
 * so the repeat is not a duplicate create.
 */
public class RespondWithReplayedOwner {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response) throws NotFoundException {
        Owner owner = Lookups.findOrNotFound(() -> ownerRepository.findById(ownerId),
                "Owner not found: " + ownerId);
        response.send(ownerMapper.toOwnerDto(owner));
    }
}
