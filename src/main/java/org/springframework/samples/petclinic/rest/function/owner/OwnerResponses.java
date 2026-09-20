package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Builds the {@link OwnerDto} for a single-owner response. Beyond the fields the
 * {@link OwnerMapper} derives from the owner alone, a response also carries fields that depend on
 * the wider owner population — the bulk-signup and capacity warnings and the composite risk flag —
 * so every single-owner responder (created / updated / fetched / replayed) assembles the DTO here
 * rather than repeating this enrichment.
 */
final class OwnerResponses {

    private OwnerResponses() {
    }

    /** Map {@code owner} to its DTO and add the response-time, population-dependent fields. */
    static OwnerDto toDto(Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(DailyRegistrations.isBulkSignup(ownerRepository, owner.getRegistrationDate()));
        dto.setCapacityWarning(Cities.isApproachingCapacity(ownerRepository, owner.getCity()));
        dto.setRiskFlag(RiskFlag.of(owner, ownerRepository));
        return dto;
    }
}
