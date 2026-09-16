package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The single duplicate check for create-owner: rejects a request whose derived
 * {@code identityKey} exactly equals an existing owner's, responding 409 via
 * {@link DuplicateOwnerException}. Consolidates the former separate telephone, email and
 * household checks into one {@link OwnerIdentities key}.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} so the
 * request telephone is already E.164 and its email lower-cased. The household component
 * matches {@link AssignOwnerHousehold}: an owner opting in with {@code sharesHousehold}
 * gets the shared {@code householdId}, otherwise it is absent.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        String householdId = Boolean.TRUE.equals(request.getSharesHousehold())
                ? OwnerHouseholds.id(request.getLastName(), request.getAddress())
                : null;
        String identityKey = OwnerIdentities.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentities.of(existing))) {
                throw new DuplicateOwnerException(identityKey);
            }
        }
    }
}
