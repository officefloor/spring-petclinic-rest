package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft (possible) duplicate. The new owner is not a hard duplicate — its
 * {@link Owner#getIdentityKey() identityKey} is unique, otherwise
 * {@link CheckOwnerIdentityUnique} would already have rejected it with 409 — but it may still be a
 * <em>likely</em> duplicate of an existing owner: same {@code lastName} (compared
 * case-insensitively) and same {@code postcode}, yet a different {@code telephone}.
 *
 * <p>When such an existing owner is found, the new owner is still created but carries
 * {@code possibleDuplicate = true} and {@code possibleDuplicateOf} set to that existing owner's id;
 * when several match, the earliest (lowest id) is used for a deterministic result. Otherwise
 * {@code possibleDuplicate = false} and {@code possibleDuplicateOf} is left {@code null}.
 *
 * <p>Runs after {@link CheckOwnerIdentityUnique} (hard duplicates already rejected) and before
 * {@link SaveOwner}, so the scan sees only the owners already persisted, not the new owner itself.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        if (postcode == null) {
            return; // no postcode to match on -> never a possible duplicate
        }
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (!equalsIgnoreCase(lastName, existing.getLastName())) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (equals(telephone, existing.getTelephone())) {
                continue; // same telephone is not a soft match (a hard match was already rejected)
            }
            if (match == null || lowerId(existing.getId(), match.getId())) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static boolean lowerId(Integer candidate, Integer current) {
        if (candidate == null) {
            return false;
        }
        return current == null || candidate < current;
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    private static boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
