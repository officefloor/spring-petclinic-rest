package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.IdentityKey;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Soundex;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft (non-hard) duplicate. Runs after {@link CheckOwnerIdentityUnique} — so any
 * exact identity duplicate has already been rejected with 409 — and before {@link SaveOwner},
 * so the flags are computed against the owners that existed before this create and are
 * persisted with the new owner.
 *
 * <p>An owner is a possible duplicate when its {@code identityKey} <em>differs</em> from an
 * existing owner's while its {@code soundex(lastName)} and {@code postcode} both match. This
 * is exactly the case the identity key no longer treats as a hard duplicate: same household
 * (phonetic last name + postcode) but a different telephone (hence a different key). When
 * such an existing owner is found, {@code possibleDuplicate} is set true and
 * {@code possibleDuplicateOf} to that owner's id (the lowest id when several match);
 * otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf} null. A
 * soft-deleted owner never matches, and a null postcode is never a possible duplicate.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String identityKey = IdentityKey.of(owner);
        String soundex = Soundex.of(owner.getLastName());
        String postcode = owner.getPostcode();

        Owner match = null;
        if (postcode != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.isDeleted()) {
                    continue; // a soft-deleted owner is not a possible duplicate match
                }
                if (postcode.equals(existing.getPostcode())
                        && soundex.equals(Soundex.of(existing.getLastName()))
                        && !identityKey.equals(IdentityKey.of(existing))) {
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
