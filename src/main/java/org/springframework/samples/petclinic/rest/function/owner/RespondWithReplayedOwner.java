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
 * The {@code replay} branch of an idempotent create: loads the owner originally created for the
 * request's idempotency key (its id arrives as the flow {@code @Parameter}) and responds {@code 200}
 * with it, so a repeated create returns the original owner rather than creating a duplicate.
 */
public class RespondWithReplayedOwner {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response) throws NotFoundException {
        Owner owner = Lookups.findOrNotFound(() -> ownerRepository.findById(ownerId),
                "Owner not found: " + ownerId);
        response.send(ownerMapper.toOwnerDto(owner));
    }
}
