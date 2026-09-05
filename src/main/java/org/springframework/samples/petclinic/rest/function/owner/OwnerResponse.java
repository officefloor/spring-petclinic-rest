package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Builds the enriched {@link OwnerDto} returned for a single owner: the mapped owner plus the
 * derived bulk-signup and capacity warnings and the risk flag. Both {@link RespondWithOwner}
 * (200) and {@link RespondWithOwnerCreated} (201) return exactly this body and differ only in
 * status, so the enrichment lives here rather than being copied into each responder.
 */
public final class OwnerResponse {

    private OwnerResponse() {
    }

    /** The mapped owner DTO with the bulk-signup and capacity warnings and risk flag applied. */
    public static OwnerDto enriched(Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(BulkSignup.warning(ownerRepository));
        dto.setCapacityWarning(CityCapacity.warning(owner, ownerRepository));
        dto.setRiskFlag(RiskFlag.of(owner, ownerRepository));
        return dto;
    }
}
