package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's soft-match flags. A new owner is flagged as a possible duplicate when
 * it is NOT a hard identity duplicate — its {@link Owner#getIdentityKey() identity key}
 * differs from the existing owner's — yet it shares that owner's Soundex last name
 * ({@link Owner#soundex(String)}) and postcode: in that case {@code possibleDuplicate} is
 * {@code true} and {@code possibleDuplicateOf} is the matching owner's id. Otherwise
 * {@code possibleDuplicate} is {@code false} and {@code possibleDuplicateOf} is null.
 *
 * <p>Because the telephone is part of the identity key, two owners with the same last name
 * and postcode but different telephones have different keys, so this is where they surface:
 * created, not rejected, and flagged as a soft match here.
 *
 * <p>A declared household member (the request set {@code sharesHousehold}) is never a
 * suspected duplicate: it deliberately shares a last name and postcode with the rest of its
 * household, so it is left unflagged.
 *
 * <p>Runs before the owner is saved and over the existing owners only (the new owner is not
 * yet persisted); when more than one existing owner matches, the lowest id wins so the
 * result is deterministic.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        String soundex = Owner.soundex(owner.getLastName());
        String postcode = owner.getPostcode();
        String identityKey = owner.getIdentityKey();
        Owner match = null;
        // A declared household member deliberately shares a last name and postcode, so it
        // is never a suspected duplicate: skip the scan and leave the flags cleared below.
        if (!Boolean.TRUE.equals(request.getSharesHousehold()) && postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getId() == null) {
                    continue;
                }
                // A soft-deleted owner is not a real match, so it never triggers the flag.
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    continue;
                }
                // A hard identity duplicate (same key) is a 409 upstream, not a soft match:
                // flag only when the key differs but the Soundex last name and postcode match.
                if (!identityKey.equals(existing.getIdentityKey())
                        && soundex.equals(Owner.soundex(existing.getLastName()))
                        && postcode.equals(existing.getPostcode())) {
                    if (match == null || existing.getId() < match.getId()) {
                        match = existing;
                    }
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
        else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }
}
