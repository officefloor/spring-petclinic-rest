package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft duplicate: an owner that passed the hard-duplicate check (see
 * {@link RequireUniqueIdentity}) but still looks like it may duplicate an existing one because
 * it shares that owner's lastName (compared case-insensitively) and postcode while giving a
 * different telephone. Sets {@link Owner#getPossibleDuplicate() possibleDuplicate} true and
 * {@link Owner#getPossibleDuplicateOf() possibleDuplicateOf} to the matching owner's id;
 * otherwise leaves the flag false and no reference.
 *
 * <p>Runs before {@link SaveOwner} so it compares only against already-stored owners and never
 * itself. When several owners match, the lowest id is chosen for a deterministic reference.
 */
public class FlagOwnerPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer matchId = null;
        if (owner.getPostcode() != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (sameName(existing.getLastName(), owner.getLastName())
                        && owner.getPostcode().equals(existing.getPostcode())
                        && !sameTelephone(existing.getTelephone(), owner.getTelephone())
                        && (matchId == null || existing.getId() < matchId)) {
                    matchId = existing.getId();
                }
            }
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    private static boolean sameName(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    private static boolean sameTelephone(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
