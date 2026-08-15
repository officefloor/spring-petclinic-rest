package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's soft-match flags {@code possibleDuplicate} / {@code possibleDuplicateOf}. A
 * hard duplicate (identical {@link OwnerIdentityKey}) has already been rejected with 409 by {@link
 * CheckOwnerIdentityUnique}, so every owner reaching this step is being created. This step flags the
 * weaker overlap: an owner that shares an existing owner's {@code lastName} (compared
 * case-insensitively) and {@code postcode} but carries a <em>different</em> telephone. When such a
 * match exists {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to the matching
 * owner's id (the lowest id when several match, for determinism); otherwise {@code possibleDuplicate}
 * is false and {@code possibleDuplicateOf} left absent.
 *
 * <p>Runs after {@link BuildOwner} has produced the {@link Owner} and before {@link SaveOwner}
 * persists it, so {@link OwnerRepository#findAll()} returns only the pre-existing owners and the new
 * owner never matches itself. Mutates the entity in place so {@link SaveOwner} stores the values and
 * later reads return them.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        Integer matchId = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner other : ownerRepository.findAll()) {
                if (equalsIgnoreCase(owner.getLastName(), other.getLastName())
                        && postcode.equals(other.getPostcode())
                        && !equals(owner.getTelephone(), other.getTelephone())) {
                    if (matchId == null || (other.getId() != null && other.getId() < matchId)) {
                        matchId = other.getId();
                    }
                }
            }
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return (a == null) ? (b == null) : a.equalsIgnoreCase(b);
    }

    private static boolean equals(String a, String b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
}
