package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The duplicate block of the create-owner pipeline: rejects the request with a 409 (via
 * {@code DuplicateIdentityExceptionHandler}) on an <b>identity duplicate</b> — an exact match on the
 * whole {@link IdentityKey} ({@code telephone|email|householdId}) — because that is the same person.
 *
 * <p>Sharing a household ({@code (lastName, postcode)} through the deterministic
 * {@link HouseholdKey#householdId}) is <em>not</em> in itself a duplicate: a household may have several
 * members, so distinct people ({@link IdentityKey} differs, e.g. a different telephone) in the same
 * household are all allowed. A new member's membershipLevel is then capped relative to the existing
 * members by {@link OwnerHousehold}; it is no longer necessary to declare {@code sharesHousehold}.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail}, so the candidate's
 * telephone and email are already normalized. The new owner is not persisted yet, so every match found
 * is another owner.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = HouseholdKey.householdId(owner.getLastName(), owner.getPostcode());
        String candidate = IdentityKey.of(owner.getTelephone(), owner.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (!DuplicateCandidates.isCandidate(existing)) {
                continue; // a soft-deleted owner no longer blocks a duplicate or identity match
            }
            if (candidate.equals(IdentityKey.ofOwner(existing))) {
                throw new DuplicateIdentityException(candidate);
            }
        }
    }
}
