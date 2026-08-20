package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The single duplicate check for a create-owner request: it consolidates the former separate
 * telephone, email and household checks into one comparison of the whole derived
 * {@link OwnerIdentityKey identity key}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}).
 *
 * <p>Runs after the telephone and email have been normalized and after
 * {@link DetermineOwnerHousehold} has resolved any shared {@code householdId}, so the request's
 * key is built from the same normalized parts as each existing owner's key. A create is rejected
 * with a 409 only when the request's whole identity key equals an existing owner's — so two
 * members of the same household with different telephones (different keys) are both allowed.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, @Val HouseholdId householdId,
            OwnerRepository ownerRepository) throws DuplicateOwnerIdentityException {
        String identityKey = OwnerIdentityKey.of(request.getTelephone(), request.getEmail(),
                householdId == null ? null : householdId.value());
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentityKey.forOwner(existing))) {
                throw new DuplicateOwnerIdentityException(identityKey);
            }
        }
    }
}
