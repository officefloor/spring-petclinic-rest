package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The single duplicate check for creating owners. It derives the request's {@code identityKey}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, see
 * {@link OwnerIdentityKey}) and rejects with 409 via {@link DuplicateOwnerException} only when that
 * whole key equals an existing owner's. This replaces the former separate telephone, email and
 * household checks: they are all expressed through this one key. Because the normalized telephone is
 * part of the key, two members of the same household with different telephones have different keys
 * and are both allowed.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} — the request already
 * holds an E.164 telephone and a lower-cased email — and before {@link BuildOwner}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        String householdId = OwnerIdentityKey.prospectiveHouseholdId(request, ownerRepository);
        String identityKey = OwnerIdentityKey.build(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new DuplicateOwnerException(identityKey);
            }
        }
    }
}
