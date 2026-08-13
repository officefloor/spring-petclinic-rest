package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Idempotent-replay responder: returns the owner originally created under the request's
 * {@code Idempotency-Key} with 200 (not 201), reached via the {@code existing} branch of
 * {@link CheckIdempotencyKey}. The owner arrives as the flow's {@code @Parameter}, having
 * been loaded in the check step, so no create work runs on this path.
 */
public class RespondWithExistingOwner {

    public void service(@Parameter Owner owner, OwnerMapper ownerMapper,
            OwnerRepository ownerRepository, ObjectResponse<OwnerDto> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(BulkSignupWarning.forToday(ownerRepository));
        dto.setCapacityWarning(CapacityWarning.forCity(owner, ownerRepository));
        response.send(dto);
    }
}
