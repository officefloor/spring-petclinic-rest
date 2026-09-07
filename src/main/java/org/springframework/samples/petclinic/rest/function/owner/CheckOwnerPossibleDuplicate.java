package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft duplicate. Runs after {@link CheckOwnerIdentityUnique} has already rejected hard
 * duplicates, so the owner reaching this step has a distinct {@link OwnerIdentity identity key}. When
 * an existing owner has a DIFFERENT identity key but shares the new owner's {@code soundex(lastName)}
 * and postcode, the new owner is still created but marked with
 * {@link Owner#setPossibleDuplicate(boolean)} true and {@link Owner#setPossibleDuplicateOf(Integer)}
 * set to that owner's id. When more than one existing owner matches, the one with the lowest id is
 * chosen for a stable result. Otherwise {@code possibleDuplicate} stays false and
 * {@code possibleDuplicateOf} absent.
 *
 * <p>Because the telephone is part of the identity key, two owners with the same last name and
 * postcode but different telephones are no longer a hard duplicate — they surface here as a soft
 * match rather than a 409.
 *
 * <p>A declared household member ({@code sharesHousehold} true, which shares the new owner's
 * lastName and postcode) is deliberately NOT flagged: a declared member is not a suspected
 * duplicate.
 */
public class CheckOwnerPossibleDuplicate {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold,
            OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return; // a declared household member is not a suspected duplicate
        }
        String soundex = Soundex.of(owner.getLastName());
        String postcode = owner.getPostcode();
        String identityKey = OwnerIdentity.key(owner);
        if (postcode == null || postcode.isBlank()) {
            return; // no postcode to match on
        }
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                continue; // skip the owner being created
            }
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is not a soft-match either
            }
            if (identityKey.equals(OwnerIdentity.key(existing))) {
                continue; // an equal identity key is a hard duplicate, not a soft match
            }
            if (!soundex.equals(Soundex.of(existing.getLastName()))) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (match == null || existing.getId() < match.getId()) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }
}
