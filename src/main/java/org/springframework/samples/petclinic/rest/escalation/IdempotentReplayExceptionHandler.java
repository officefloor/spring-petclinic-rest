package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Handles an {@link IdempotentReplayException} from {@code POST /api/owners} by loading the owner the
 * original create produced and returning it with 200 OK — the idempotent repeat sees the same owner
 * it would have received the first time, and no duplicate is created.
 */
public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<ResponseEntity<OwnerDto>> response) {
        Owner owner = ownerRepository.findById(ex.getOwnerId());
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        response.send(ResponseEntity.ok(dto));
    }
}
