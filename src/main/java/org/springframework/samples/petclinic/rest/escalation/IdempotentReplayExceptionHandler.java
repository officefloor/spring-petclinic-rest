package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Responds 200 OK with the originally created owner when a create repeats with an already-seen
 * {@code Idempotency-Key}, so the repeat is a no-op replay rather than a duplicate create.
 */
public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex, OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(ex.getOwner());
        response.send(ResponseEntity.ok(dto));
    }
}
