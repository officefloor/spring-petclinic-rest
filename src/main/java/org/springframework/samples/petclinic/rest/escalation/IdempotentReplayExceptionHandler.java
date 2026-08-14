package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Handles {@link IdempotentReplayException} by responding 200 with the owner that the seen
 * {@code Idempotency-Key} originally created, so a repeated create is a replay rather than a
 * duplicate (which would otherwise 409).
 */
public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex, OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(ex.getOwner());
        response.send(ResponseEntity.ok(dto));
    }
}
