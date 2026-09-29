package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The duplicate block of the create-owner pipeline: rejects the request with a 409 (via
 * {@code DuplicateIdentityExceptionHandler}) in either of two cases.
 *
 * <ol>
 * <li><b>Household duplicate.</b> The household is now keyed on {@code (lastName, postcode)} through the
 * deterministic {@link HouseholdKey#householdId}, so an existing owner with the same household means the
 * new owner is a second member of it. That is rejected — <em>unless</em> the request sets
 * {@code sharesHousehold}, which bypasses this block so a declared household member may be created.</li>
 * <li><b>Identity duplicate.</b> An exact match on the whole {@link IdentityKey}
 * ({@code telephone|email|householdId}) is always rejected, even for a declared member: it is the same
 * person, not merely the same household.</li>
 * </ol>
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail}, so the candidate's
 * telephone and email are already normalized. The new owner is not persisted yet, so every match found
 * is another owner.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = HouseholdKey.householdId(owner.getLastName(), owner.getPostcode());
        boolean sharesHousehold = Boolean.TRUE.equals(owner.getSharesHousehold());
        String candidate = IdentityKey.of(owner.getTelephone(), owner.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (candidate.equals(IdentityKey.ofOwner(existing))) {
                throw new DuplicateIdentityException(candidate);
            }
            if (!sharesHousehold && householdId.equals(HouseholdKey.householdId(existing))) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
