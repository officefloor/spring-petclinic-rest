package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft (possible) duplicate. An owner that is not a hard identity duplicate (that check
 * runs earlier and rejects with 409) but that shares an existing owner's {@code lastName}
 * (case-insensitive) and {@code postcode} is still created, with {@code possibleDuplicate} set true
 * and {@code possibleDuplicateOf} set to the matching owner's id. When several owners match, the
 * earliest (lowest id) is chosen. Otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is absent.
 *
 * <p>Runs before {@link SaveOwner}, so the new owner is not yet persisted and only pre-existing
 * owners are considered.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        Integer matchId = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                    continue; // never match the new owner against itself
                }
                if (equalsIgnoreCase(existing.getLastName(), lastName)
                        && postcode.equals(existing.getPostcode())) {
                    if (matchId == null || existing.getId() < matchId) {
                        matchId = existing.getId();
                    }
                }
            }
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
