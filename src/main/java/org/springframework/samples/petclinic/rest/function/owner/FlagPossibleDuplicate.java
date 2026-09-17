package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft duplicate: a new owner that passed the hard-duplicate check (see
 * {@link EnsureUniqueIdentity}) but still shares an existing owner's last name (compared
 * case-insensitively) and postcode while giving a different telephone. Such a request is allowed but
 * marked for follow-up: the built {@link Owner} records {@code possibleDuplicate} true and
 * {@code possibleDuplicateOf} the matching owner's id (the earliest match when several exist).
 * When no such owner exists {@code possibleDuplicate} is false and {@code possibleDuplicateOf} stays
 * absent. Runs before {@link SaveOwner}, so the new owner is not yet persisted and is never matched
 * against itself. Mutates the built {@link Owner} in place.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        String postcode = trimmed(owner.getPostcode());
        if (postcode == null) {
            return;
        }
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getLastName().equalsIgnoreCase(existing.getLastName())
                    && postcode.equals(trimmed(existing.getPostcode()))
                    && !owner.getTelephone().equals(existing.getTelephone())) {
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

    private static String trimmed(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }
}
