package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Runs after {@link NormalizeOwnerTelephone} in the create-owner pipeline: rejects the request with a
 * 409 (via {@code DuplicateTelephoneExceptionHandler}) when the normalized telephone is already used by
 * any other owner. Comparison strips every non-digit character from both sides so an owner stored with
 * differently formatted digits still counts as a match. The new owner is not persisted yet, so every
 * match found is another owner.
 */
public class RequireUniqueTelephone {

    public void service(@Val OwnerFieldsDto owner, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        // NormalizeOwnerTelephone already ran, so this is a normalized 10-digit value. Stored owners
        // are normalized through the same shared rule for owners created outside this pipeline.
        String digits = owner.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (digits.equals(TelephoneNormalizer.normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(digits);
            }
        }
    }
}
