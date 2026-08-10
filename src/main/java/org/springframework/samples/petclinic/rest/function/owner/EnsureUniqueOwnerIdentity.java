package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} (so the request telephone
 * is E.164 and the email lower-cased) and before {@link BuildOwner}. Consolidates all duplicate
 * detection into a single derived {@code identityKey} = {@code normalizedTelephone + '|' + (email or
 * empty) + '|' + (householdId or empty)}, and rejects the request with {@link DuplicateIdentityException}
 * (handled as 409) only when the new owner's WHOLE identity key equals an existing owner's.
 *
 * <p>Because the telephone is part of the key, two members of the same household (same householdId)
 * with different telephones have different keys and are both allowed; only an exact full-key match is
 * a duplicate. The new owner's household component is the id it will be assigned by
 * {@link AssignOwnerHousehold}: the deterministic household id when the request opts in with
 * {@code sharesHousehold: true}, otherwise empty.
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = Boolean.TRUE.equals(request.getSharesHousehold())
                ? OwnerIdentity.householdId(request.getLastName(), request.getAddress())
                : null;
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            String existingKey = OwnerIdentity.key(OwnerIdentity.toE164(existing.getTelephone()),
                    OwnerIdentity.normalizeEmail(existing.getEmail()), existing.getHouseholdId());
            if (identityKey.equals(existingKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
