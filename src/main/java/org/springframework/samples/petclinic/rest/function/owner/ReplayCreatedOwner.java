package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Replay branch of {@code POST /api/owners}: a repeat with an already-seen {@code Idempotency-Key}
 * reloads the owner first created under that key and returns it with 200 (not 201), so a retried
 * create is safe and yields the same owner rather than a duplicate.
 */
public class ReplayCreatedOwner {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository,
            OwnerMapper ownerMapper, ObjectResponse<OwnerDto> response) {
        Owner owner = ownerRepository.findById(ownerId);
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(BulkSignup.warning(ownerRepository));
        dto.setCapacityWarning(CityCapacity.warning(owner, ownerRepository));
        response.send(dto);
    }
}
