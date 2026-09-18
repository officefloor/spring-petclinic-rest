package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Handles {@link IdempotentReplayException} by responding 200 with the owner originally created for
 * the request's idempotency key — the idempotent-replay outcome of {@code POST /api/owners}.
 */
public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response) {
        Owner owner = ownerRepository.findById(ex.getOwnerId());
        response.send(ownerMapper.toOwnerDto(owner));
    }
}
