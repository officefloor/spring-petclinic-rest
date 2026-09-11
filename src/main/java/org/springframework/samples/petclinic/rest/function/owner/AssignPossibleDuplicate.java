package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft (non-hard) duplicate. Runs after {@link CheckOwnerIdentityUnique} — so a
 * household duplicate has already been rejected with 409 — and before {@link SaveOwner}, so
 * the flags are computed against the owners that existed before this create and are
 * persisted with the new owner.
 *
 * <p>A <em>declared</em> household member (a request that opted in via
 * {@code sharesHousehold}) is never a suspected duplicate: it is deliberately created into
 * an existing household, so it is left un-flagged.
 *
 * <p>Otherwise an owner is a possible duplicate when it shares an existing owner's
 * {@code lastName} (compared case-insensitively) and {@code postcode} while carrying a
 * <em>different</em> telephone. When such an existing owner is found,
 * {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to that owner's id
 * (the lowest id when several match); otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} null. A null postcode never matches, so it is never a possible
 * duplicate.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return; // a declared household member is not a suspected duplicate
        }
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();

        Owner match = null;
        if (postcode != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.isDeleted()) {
                    continue; // a soft-deleted owner is not a possible duplicate match
                }
                if (postcode.equals(existing.getPostcode())
                        && equalsIgnoreCase(lastName, existing.getLastName())
                        && !equals(telephone, existing.getTelephone())) {
                    if (match == null || existing.getId() < match.getId()) {
                        match = existing;
                    }
                }
            }
        }

        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
        else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    private static boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
