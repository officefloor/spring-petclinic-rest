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
 * Responds 200 with the originally created owner when a create repeats with an already-seen
 * {@code Idempotency-Key}. The owner id is carried on the {@link IdempotentReplayException}; the
 * current owner is re-read so the response reflects its stored state.
 */
public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<ResponseEntity<OwnerDto>> response) {
        Owner owner = ownerRepository.findById(ex.getOwnerId());
        response.send(ResponseEntity.status(HttpStatus.OK).body(ownerMapper.toOwnerDto(owner)));
    }
}
