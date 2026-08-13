package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft (non-hard) duplicate on the owner being created.
 *
 * <p>A hard duplicate — an existing owner with the same whole {@code identityKey} — has
 * already been rejected with 409 by {@link EnsureUniqueIdentity}. The soft-match key
 * (same {@code lastName}, compared case-insensitively, and same {@code postcode}, with a
 * <em>different</em> telephone) is now exactly the household key: a non-declared owner
 * sharing it has already been rejected as a household duplicate by
 * {@link EnsureUniqueHousehold}, so the only owner that reaches this step with such a
 * match is a <em>declared</em> household member ({@code sharesHousehold: true}). A
 * declared member is not a suspected duplicate, so it is never flagged.
 *
 * <p>For any other owner the soft match, when present, still sets {@code possibleDuplicate}
 * true and {@code possibleDuplicateOf} to the matching owner's id (the lowest id when
 * several match, for determinism); otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is absent.
 *
 * <p>Runs after {@link EnsureUniqueIdentity} and before {@link SaveOwner} in the
 * {@code POST /api/owners} pipeline, so the new owner is not yet persisted and is never
 * compared against itself; the flags are persisted with the new owner and returned by
 * later reads. It mutates the built {@link Owner} in place (see {@code @Val} semantics).
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
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
