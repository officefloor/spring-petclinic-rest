package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a create as a soft duplicate. Runs before {@link SaveOwner}, so
 * {@link OwnerRepository#findAll()} returns only the owners that predate this create. A new owner
 * that {@link EnsureIdentityUnique} already cleared as not a hard duplicate is nonetheless a
 * <em>possible</em> duplicate when an existing owner shares its normalized lastName and its postcode
 * but carries a different telephone. On such a match it sets {@code possibleDuplicate} true and
 * {@code possibleDuplicateOf} to the matching owner's id (the lowest id when several match); the
 * owner is still created. With no match it sets {@code possibleDuplicate} false and leaves
 * {@code possibleDuplicateOf} null.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        String postcode = owner.getPostcode();
        // A soft match keys on a shared postcode; an owner with no postcode cannot share one.
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String lastName = OwnerIdentity.normalizeName(owner.getLastName());
        String telephone = OwnerIdentity.canonicalTelephone(owner.getTelephone());

        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (!lastName.equals(OwnerIdentity.normalizeName(existing.getLastName()))) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (telephone.equals(OwnerIdentity.canonicalTelephone(existing.getTelephone()))) {
                continue; // same telephone is not the "different telephone" soft match
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
