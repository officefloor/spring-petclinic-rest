package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft (non-hard) duplicate on the owner being created.
 *
 * <p>A hard duplicate — an existing owner with the same whole {@code identityKey} — has
 * already been rejected with 409 by {@link EnsureUniqueIdentity}. This step handles the
 * softer case: the new owner is <em>not</em> a hard duplicate but still shares an
 * existing owner's {@code lastName} (compared case-insensitively) and {@code postcode}
 * while carrying a <em>different</em> telephone. When such a match exists the owner is
 * still created, but with {@code possibleDuplicate} true and {@code possibleDuplicateOf}
 * set to the matching owner's id (the lowest id when several match, for determinism);
 * otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is absent.
 *
 * <p>Runs after {@link EnsureUniqueIdentity} and before {@link SaveOwner} in the
 * {@code POST /api/owners} pipeline, so the new owner is not yet persisted and is never
 * compared against itself; the flags are persisted with the new owner and returned by
 * later reads. It mutates the built {@link Owner} in place (see {@code @Val} semantics).
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        if (postcode == null) {
            return;
        }
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (equalsIgnoreCase(lastName, existing.getLastName())
                    && postcode.equals(existing.getPostcode())
                    && !equals(telephone, existing.getTelephone())
                    && (match == null || existing.getId() < match.getId())) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    private static boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
