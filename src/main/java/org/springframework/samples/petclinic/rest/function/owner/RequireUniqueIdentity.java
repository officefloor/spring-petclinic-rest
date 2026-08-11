package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Single duplicate-detection step of {@code POST /api/owners}: rejects the request 409 when the new
 * owner's whole {@code identityKey} equals an existing owner's. The key
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)}) subsumes the
 * former separate telephone, email and household checks &mdash; two members of the same household
 * with different telephones now have different keys and are both allowed; only an exact full-key
 * match is a duplicate.
 *
 * <p>Runs after the normalize steps (so telephone is E.164 and email lower-cased) and before
 * {@link BuildOwner} so no owner is persisted on conflict. The would-be {@code householdId} is
 * derived exactly as {@link AssignHouseholdId} will assign it &mdash; from the normalized last name
 * and address when the request opts in with {@code sharesHousehold: true}, otherwise absent.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = Boolean.TRUE.equals(request.getSharesHousehold())
                ? OwnerIdentity.householdId(request.getLastName(), request.getAddress())
                : null;
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            String existingKey = OwnerIdentity.key(existing.getTelephone(), existing.getEmail(),
                    existing.getHouseholdId());
            if (identityKey.equals(existingKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
