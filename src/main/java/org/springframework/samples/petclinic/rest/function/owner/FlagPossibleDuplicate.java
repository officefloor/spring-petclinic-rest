package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Computes this owner's {@code possibleDuplicate} / {@code possibleDuplicateOf} response fields, on both
 * the create and read pipelines.
 *
 * <p>A possible duplicate is a soft near-match: another non-deleted owner that has a <em>different</em>
 * {@link Owner#getIdentityKey() identity key} (so it is not the same identity — an equal key is a hard
 * duplicate rejected by {@link CheckIdentityUnique}) yet shares this owner's last-name
 * {@link Owner#soundex(String) Soundex} and postcode. Because the telephone is part of the identity key,
 * two owners with the same last name and postcode but different telephones differ in key and so surface
 * here as a soft match. When several owners match, {@code possibleDuplicateOf} is the earliest (lowest id);
 * no match leaves the fields {@code false} / {@code null}. Computed per request, not persisted, and
 * recomputed identically on each read.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        String soundex = Owner.soundex(owner.getLastName());
        String postcode = Household.normalizePostcode(owner.getPostcode());
        String identityKey = owner.getIdentityKey();

        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue;
            }
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never a duplicate of itself
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                continue; // same identity is a hard duplicate, not a soft near-match
            }
            if (soundex.equals(Owner.soundex(existing.getLastName()))
                    && postcode.equals(Household.normalizePostcode(existing.getPostcode()))) {
                if (matchId == null || existing.getId() < matchId) {
                    matchId = existing.getId();
                }
            }
        }

        if (matchId != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(matchId);
        }
    }
}
