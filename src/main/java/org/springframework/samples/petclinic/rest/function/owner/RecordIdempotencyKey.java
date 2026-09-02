package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Runs after the owner is saved. Remembers the created owner against the request's
 * {@code Idempotency-Key} so a later repeat with the same key replays this owner. The DTO is built
 * here, under the same governance, so the replay handler needs no further database access.
 */
public class RecordIdempotencyKey {

    public void service(@Val IdempotencyToken token, @Val Owner owner, OwnerMapper ownerMapper,
            OwnerRepository ownerRepository, IdempotencyStore store) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        HouseholdTier.applyGold(owner, ownerRepository, dto);
        dto.setBulkSignupWarning(BulkSignup.warned(ownerRepository));
        dto.setCapacityWarning(CityCapacity.approaching(owner, ownerRepository));
        store.record(token.key(), dto);
    }
}
