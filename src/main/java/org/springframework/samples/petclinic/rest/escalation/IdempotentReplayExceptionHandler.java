package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.function.owner.OwnerResponses;

/**
 * Answers an idempotent-create replay: loads the owner originally created for the repeated
 * {@code Idempotency-Key} and returns it with 200, the same representation a fresh create would.
 */
public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<ResponseEntity<OwnerDto>> response) {
        Owner owner = ownerRepository.findById(ex.getOwnerId());
        response.send(ResponseEntity.ok(OwnerResponses.toDto(owner, ownerMapper, ownerRepository)));
    }
}
