package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The single duplicate check on {@code POST /api/owners}: consolidates the former separate
 * telephone, email and household checks into one comparison of the derived {@link OwnerIdentityKey}
 * ({@code <normalizedTelephone>|<email>|<householdId>}). Rejects with 409 via
 * {@link DuplicateIdentityException} only when the new owner's WHOLE identity key equals an existing
 * owner's; because the telephone is part of the key, two members of the same household with
 * different telephones have different keys and are both allowed. Runs before {@link BuildOwner} so
 * no owner is created on conflict; it resolves the householdId the request would receive so its key
 * matches the one assigned and returned afterwards.
 */
public class CheckUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = OwnerHousehold.resolve(request, ownerRepository);
        String identityKey = OwnerIdentityKey.of(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
