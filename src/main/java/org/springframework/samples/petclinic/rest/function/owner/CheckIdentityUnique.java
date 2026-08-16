package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects creating an owner whose {@code identityKey} exactly equals an existing owner's, so the
 * create endpoint responds 409 instead of storing a duplicate. This single check replaces the former
 * separate telephone, email and household checks: all duplicate detection is now expressed through
 * the one key {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId} (see
 * {@link OwnerIdentity}).
 *
 * <p>Only a whole-key match is a duplicate. Because the telephone is part of the key, two members of
 * the same household (same {@code householdId}) with different telephones have different keys and are
 * both allowed. The new owner's household component matches what {@link AssignHousehold} will assign:
 * a shared id when the request opts in with {@code sharesHousehold: true}, otherwise empty.
 *
 * <p>Runs after {@link ValidateOwner} (which normalizes and publishes the body) and before
 * {@link BuildOwner}, comparing against every existing owner's identity key.
 */
public class CheckIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = Boolean.TRUE.equals(request.getSharesHousehold())
                ? OwnerIdentity.householdIdFor(request.getLastName(), request.getAddress())
                : "";
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
