package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft duplicate before the new owner is saved. A hard duplicate (matching
 * {@link IdentityKey}) has already been rejected by {@link RequireUniqueIdentity}; this
 * records the id of an existing owner whose {@code soundex(lastName)} and postcode match but
 * whose identity key differs, so the response can surface {@code possibleDuplicate}/
 * {@code possibleDuplicateOf}. A declared household member ({@code sharesHousehold}) is not
 * a suspected duplicate, and there is no match when there is no postcode. */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String soundex = IdentityKey.soundex(owner.getLastName());
        String key = keyOf(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue;
            }
            if (soundex.equals(IdentityKey.soundex(existing.getLastName()))
                    && postcode.equals(existing.getPostcode()) && !key.equals(keyOf(existing))) {
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }

    private static String keyOf(Owner owner) {
        return IdentityKey.of(owner.getTelephone(), owner.getEmail(), owner.getLastName(),
                owner.getPostcode());
    }
}
