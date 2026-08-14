package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Replays the originally created owner with 200 when a create-owner request repeats an already-seen
 * {@code Idempotency-Key}. Loads the owner recorded against the key and returns it, so the repeated
 * create is a no-op that yields the same owner rather than a duplicate.
 */
public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<ResponseEntity<OwnerDto>> response) {
        Owner owner = ownerRepository.findById(ex.getOwnerId());
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        response.send(ResponseEntity.status(HttpStatus.OK).body(dto));
    }
}
