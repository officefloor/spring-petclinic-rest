package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Idempotent-replay branch of {@code POST /api/owners}: responds 200 with the owner originally
 * created for a repeated {@code Idempotency-Key}. The owner id arrives from
 * {@link RouteIdempotentCreate} as the {@code @Parameter}; the owner is re-read so the response
 * body matches a normal fetch. Uses a plain {@code ObjectResponse<OwnerDto>} so the status is 200,
 * distinguishing a replay from the 201 that a fresh create returns.
 */
public class RespondWithExistingOwner {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response) {
        Owner owner = ownerRepository.findById(ownerId);
        response.send(ownerMapper.toOwnerDto(owner));
    }
}
