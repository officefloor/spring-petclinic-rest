package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Turns an {@link IdempotentCreateReplayException} into a 200 carrying the owner the original
 * request created, so a repeated create with an already-seen {@code Idempotency-Key} is a no-op
 * that returns the existing owner rather than a 201 for a duplicate.
 */
public class IdempotentCreateReplayHandler {

    public void handle(@Parameter IdempotentCreateReplayException ex, OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        Owner owner = ex.getOwner();
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        response.send(ResponseEntity.status(HttpStatus.OK).body(dto));
    }
}
