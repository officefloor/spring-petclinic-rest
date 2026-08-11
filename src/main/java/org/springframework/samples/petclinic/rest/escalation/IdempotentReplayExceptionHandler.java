package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Replays the owner originally created under an {@code Idempotency-Key}: loads it fresh and responds
 * 200 with the same {@link OwnerDto} body a create returns, so a repeated create is a no-op read.
 */
public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response) {
        Owner owner = ownerRepository.findById(ex.getOwnerId());
        response.send(ownerMapper.toOwnerDto(owner));
    }
}
