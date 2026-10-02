package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags this owner as a <em>possible</em> (soft) duplicate. An owner is a possible duplicate when some
 * other owner shares its last name (compared case-insensitively) and postcode but has a different
 * telephone — a near match that is not a hard duplicate (a hard duplicate shares the whole identity key,
 * including the telephone, and is rejected earlier by {@link CheckIdentityUnique}).
 *
 * <p>Sets {@code possibleDuplicate} to {@code true} and {@code possibleDuplicateOf} to the matching
 * owner's id (the earliest match when several match); otherwise {@code possibleDuplicate} is {@code false}
 * and {@code possibleDuplicateOf} is {@code null}. Both are computed per request, not persisted, so the
 * flag is recomputed identically on each read. The owner itself is excluded from the comparison by id.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        if (postcode == null || postcode.isBlank()) {
            return;
        }

        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (isSelf(owner, existing)) {
                continue;
            }
            if (!equalsIgnoreCase(lastName, existing.getLastName())) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (equals(telephone, existing.getTelephone())) {
                continue; // same telephone would be a hard duplicate, not a possible one
            }
            if (match == null || isEarlier(existing, match)) {
                match = existing;
            }
        }

        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static boolean isSelf(Owner owner, Owner existing) {
        return owner.getId() != null && owner.getId().equals(existing.getId());
    }

    private static boolean isEarlier(Owner candidate, Owner current) {
        Integer a = candidate.getId();
        Integer b = current.getId();
        if (a == null) {
            return false;
        }
        if (b == null) {
            return true;
        }
        return a < b;
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    private static boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
