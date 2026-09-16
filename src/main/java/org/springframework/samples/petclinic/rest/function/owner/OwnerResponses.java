package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Builds the single-owner response DTO. Beyond the plain field mapping it stamps the
 * derived flags a single owner cannot compute alone because they depend on the other
 * owners — the bulk-signup and city-capacity warnings and the composite risk flag. Kept
 * here so every single-owner responder ({@link RespondWithOwner},
 * {@link RespondWithOwnerCreated}, {@link RespondWithOwnerUpdated}) agrees on the shape.
 */
final class OwnerResponses {

    private OwnerResponses() {
    }

    static OwnerDto toDto(Owner owner, OwnerMapper mapper, OwnerRepository repository) {
        OwnerDto dto = mapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(BulkSignup.warningRaised(repository));
        dto.setCapacityWarning(CityCapacity.warningRaised(repository, owner.getCity()));
        dto.setRiskFlag(OwnerRisk.of(owner, repository));
        return dto;
    }
}
