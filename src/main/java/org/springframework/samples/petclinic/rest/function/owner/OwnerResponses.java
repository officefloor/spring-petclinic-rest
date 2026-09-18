package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * The single place that turns an {@link Owner} into its response {@link OwnerDto}: maps the
 * entity and stamps the current bulk-signup and approaching-capacity warnings. Shared by every
 * step that returns an owner — the create (201), the read (200) and the idempotent replay (200)
 * — so all owner responses carry an identical representation.
 */
public final class OwnerResponses {

    private OwnerResponses() {
    }

    public static OwnerDto toDto(Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(BulkSignupWarning.isRaised(ownerRepository));
        dto.setCapacityWarning(CapacityWarning.isRaised(owner.getCity(), ownerRepository));
        return dto;
    }
}
