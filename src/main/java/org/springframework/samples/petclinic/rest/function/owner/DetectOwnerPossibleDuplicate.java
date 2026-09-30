package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the new owner as a possible (soft) duplicate. With duplicate detection now keyed off the
 * single {@link Owner#getIdentityKey() identityKey} (handled as a 409 in {@link DetectOwnerDuplicate}),
 * the soft signal catches the near-miss that the key deliberately lets through: an earlier non-deleted
 * owner whose {@link Owner#soundex(String) soundex(lastName)} and postcode both match this owner's,
 * but whose identity key differs.
 *
 * <p>Because the telephone (and email) are part of the identity key, two owners sharing a last name
 * and postcode but differing in telephone or email have different keys — so they are not a hard
 * duplicate (no 409); they surface here as a soft match instead.
 *
 * <p>Runs after {@link BuildOwner} and {@link DetectOwnerDuplicate} (so a hard duplicate has already
 * been rejected) and before {@link SaveOwner}, scanning only pre-existing (already persisted) owners —
 * the new owner has no id yet and is skipped. The earliest such match (lowest id) is recorded:
 * {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to that owner's id; otherwise
 * {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is left null. Both are stored on
 * the built owner so the saved and returned record carries them.
 */
public class DetectOwnerPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String soundex = Owner.soundex(owner.getLastName());
        String postcode = owner.getPostcode();
        String identityKey = owner.getIdentityKey();
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null) {
                continue; // an unsaved owner (including the new one) has no id to record
            }
            if (existing.getId().equals(owner.getId())) {
                continue; // the owner itself is never its own duplicate
            }
            if (existing.isDeleted()) {
                continue; // soft-deleted owners are ignored
            }
            if (!soundex.equals(Owner.soundex(existing.getLastName()))) {
                continue; // last names must be phonetically equal
            }
            if (!Objects.equals(postcode, existing.getPostcode())) {
                continue; // and share a postcode
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                continue; // an exact identity match is a hard duplicate, not a soft one
            }
            if (match == null || existing.getId() < match.getId()) {
                match = existing;
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
