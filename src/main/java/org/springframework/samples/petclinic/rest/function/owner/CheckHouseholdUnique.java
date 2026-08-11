package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerHouseholdConflictException;

/**
 * The household-duplicate block for {@code POST /api/owners}: because a household is keyed on the
 * computed {@code householdId} (last name + postcode), a new owner whose householdId already belongs
 * to an existing owner is a household duplicate and is rejected with 409 — <em>unless</em> it is a
 * distinct household member. A new owner is treated as a distinct member (and created) when either:
 * <ul>
 *   <li>the request declared {@code sharesHousehold}, which explicitly waives the block; or</li>
 *   <li>the new owner carries a distinguishing email — a non-blank address that no existing member
 *       of the household already uses — so it is plainly a different person, not a re-registration.</li>
 * </ul>
 * A new owner with no email (or one repeating an existing member's email) that does not declare
 * {@code sharesHousehold} is still a household duplicate and rejected with 409.
 *
 * <p>Runs after {@link AssignHouseholdId} has assigned the shared householdId and before
 * {@link SaveOwner}, so the not-yet-saved owner is not compared against itself. The full-identity
 * duplicate check ({@link CheckOwnerIdentityUnique}) is separate and still applies to a distinct
 * member, so a member repeating another member's whole identityKey is still a 409.
 */
public class CheckHouseholdUnique {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerHouseholdConflictException {
        // A declared household member bypasses the block; the identity check still guards full
        // duplicates.
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        String email = normalizeEmail(owner.getEmail());
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a new household
            }
            if (!householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            // A same-household member is a duplicate unless the new owner distinguishes itself with a
            // non-blank email that this member does not already use.
            if (email.isEmpty() || email.equals(normalizeEmail(existing.getEmail()))) {
                throw new OwnerHouseholdConflictException(
                        "An owner in household " + householdId + " already exists");
            }
        }
    }

    /** Lower-cased, trimmed email; a null or blank email normalizes to the empty string. */
    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
