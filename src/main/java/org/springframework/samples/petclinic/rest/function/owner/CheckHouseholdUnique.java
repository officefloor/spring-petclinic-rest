package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects (409) a create-owner request that would join an existing household. Because the household
 * is now keyed on the computed {@code householdId} (a pure function of the normalized last name and
 * postcode, see {@link OwnerIdentity#householdId}), owners sharing a last name and postcode are the
 * same household. A second such owner reads as the same person re-entered — and is a household
 * duplicate — only when it also carries the same email (both absent counts as the same); a genuine
 * distinct household member, told apart by a differing email, is admitted. Declaring
 * {@code sharesHousehold} bypasses the block entirely, creating a declared household member.
 *
 * <p>Runs after {@link CheckIdentityUnique} (so an exact identity collision is still a 409 even when
 * {@code sharesHousehold} is set) and within the write transaction, comparing against the owners
 * persisted so far. A missing postcode carries no household, so it never collides here.
 */
public class CheckHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member — bypass the block
        }
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // no postcode -> no household to collide with
        }
        String householdId = OwnerIdentity.householdId(request.getLastName(), postcode);
        String email = normalizeEmail(request.getEmail());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners are ignored by the household check
            }
            // A genuine distinct household member is told apart by a differing email; only a matching
            // email (both absent counts as matching) reads as the same person re-entered and is blocked.
            if (householdId.equals(existing.getHouseholdId())
                    && email.equals(normalizeEmail(existing.getEmail()))) {
                throw new DuplicateHouseholdException(householdId);
            }
        }
    }

    /** Lower-cased email, or {@code ""} when absent/blank, so the comparison is case-insensitive. */
    private static String normalizeEmail(String email) {
        return (email == null || email.isBlank()) ? "" : email.toLowerCase(Locale.ROOT);
    }
}
