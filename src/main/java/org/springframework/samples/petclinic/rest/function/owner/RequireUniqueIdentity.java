package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The duplicate block of the create-owner pipeline: rejects the request with a 409 (via
 * {@code DuplicateIdentityExceptionHandler}) on an <b>identity duplicate</b> — an exact match on the
 * whole {@link IdentityKey} (the SHA-256 of {@code telephone|email|soundex(lastName)}) — because that
 * is the same person. This single identity key is now the only duplicate rule: there is no separate
 * household-duplicate 409 keyed off the computed {@link HouseholdKey#householdId}.
 *
 * <p>Sharing a last name and postcode is <em>not</em> in itself a duplicate: two owners with the same
 * last name and postcode but different telephones have different identity keys and are both allowed —
 * the second is created and flagged as a soft match by {@link AssignOwnerPossibleDuplicate}. A new
 * member's membershipLevel is then capped relative to the existing members by {@link OwnerHousehold};
 * it is no longer necessary to declare {@code sharesHousehold}.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail}, so the candidate's
 * telephone and email are already normalized, and after {@link RequireEmailNotDisposable} so the
 * email-domain blocklist is applied first. The new owner is not persisted yet, so every match found is
 * another owner.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String candidate = IdentityKey.of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
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
