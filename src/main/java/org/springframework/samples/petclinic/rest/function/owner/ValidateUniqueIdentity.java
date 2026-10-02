package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Runs after {@link ValidateNewOwner} (which normalizes the telephone and email) and
 * {@link ResolveHousehold} (which resolves the shared {@link Household.Id}), and before
 * {@link BuildOwner}. The single duplicate-detection gate: it derives the new owner's
 * {@link OwnerIdentity identity key} and rejects the create with a 409 when any existing owner
 * has the same whole key, consolidating the former separate telephone, email and household
 * checks. Publishes nothing; it only escalates on conflict.
 */
public class ValidateUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val Household.Id householdId,
            OwnerRepository ownerRepository) throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(),
                householdId == null ? null : householdId.value());
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(
                        "Identity key " + identityKey + " is already used by another owner");
            }
        }
    }
}
