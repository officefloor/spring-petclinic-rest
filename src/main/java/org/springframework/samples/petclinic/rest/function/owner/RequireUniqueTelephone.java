package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Runs after {@link NormalizeOwnerTelephone} in the create-owner pipeline: rejects the request with a
 * 409 (via {@code DuplicateTelephoneExceptionHandler}) when the E.164 telephone is already used by any
 * other owner. Comparison converts both sides to E.164 through the same shared rule so an owner stored
 * with differently formatted digits still counts as a match. The new owner is not persisted yet, so
 * every match found is another owner.
 */
public class RequireUniqueTelephone {

    public void service(@Val OwnerFieldsDto owner, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        // NormalizeOwnerTelephone already ran, so this is an E.164 value. Stored owners are converted
        // through the same shared rule so owners created outside this pipeline still compare correctly.
        String e164 = owner.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (e164.equals(TelephoneNormalizer.toE164(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(e164);
            }
        }
    }
}
