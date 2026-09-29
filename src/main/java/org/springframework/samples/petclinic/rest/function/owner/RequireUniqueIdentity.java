package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check of the create-owner pipeline: rejects the request with a 409 (via
 * {@code DuplicateIdentityExceptionHandler}) when the new owner's whole {@link IdentityKey} equals an
 * existing owner's. This consolidates the former separate telephone, email and household checks — all
 * three are now expressed through the one key.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail}, so the candidate's
 * telephone and email are already in their normalized form. The candidate's household id comes from
 * {@link HouseholdKey#candidateHouseholdId} — the same single definition {@link AssignHousehold} uses
 * to assign it — so the key and the assigned id cannot drift apart. Because the telephone is part of
 * the key, two members of the same household with different telephones have different keys and are
 * both allowed.
 *
 * <p>The new owner is not persisted yet, so every match found is another owner.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = HouseholdKey.candidateHouseholdId(owner);
        String candidate = IdentityKey.of(owner.getTelephone(), owner.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (candidate.equals(IdentityKey.ofOwner(existing))) {
                throw new DuplicateIdentityException(candidate);
            }
        }
    }
}
