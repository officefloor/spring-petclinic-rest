package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-duplicate flagging step of {@code POST /api/owners}. Runs after {@link CheckOwnerIdentityUnique}
 * (so the new owner is already known not to be a hard, {@code identityKey} duplicate) and before
 * {@link SaveOwner}.
 *
 * <p>A new owner is a <em>possible</em> duplicate when, though not a hard duplicate, it shares an
 * existing owner's {@code lastName} (compared case-insensitively) and {@code postcode} while having a
 * different {@code telephone}. Such an owner is still created, but flagged: {@code possibleDuplicate} is
 * set true and {@code possibleDuplicateOf} to the matching existing owner's id (the lowest id when more
 * than one matches). When there is no match the owner is flagged {@code possibleDuplicate} false. The
 * new owner is still unsaved, so it is not among the owners iterated here and cannot match itself.
 */
public class CheckPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        if (postcode == null || postcode.isBlank()) {
            return; // no postcode to match on
        }

        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (OwnerMatch.equalsIgnoreCase(lastName, existing.getLastName())
                    && postcode.equals(existing.getPostcode())
                    && !OwnerMatch.equalsIgnoreCase(telephone, existing.getTelephone())) {
                if (match == null || existing.getId() < match.getId()) {
                    match = existing;
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }
}
