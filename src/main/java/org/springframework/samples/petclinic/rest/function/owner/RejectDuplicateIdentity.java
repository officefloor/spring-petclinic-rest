package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.function.common.IdentityKeys;

/**
 * The single duplicate-detection step of {@code POST /api/owners}. Runs after
 * {@link ValidateOwnerFields} has normalized the telephone and email. Every duplicate rule —
 * telephone, email and household — is now expressed through one derived {@code identityKey}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)}); the request
 * is rejected as a 409 Conflict only when its <em>whole</em> key equals an existing owner's.
 *
 * <p>The household component is the id this request would be assigned (see {@link Households#resolve},
 * mirrored by {@link AssignHousehold}), so two members of the same household with different
 * telephones have different keys and are both allowed — only an exact full-key match is a duplicate.
 */
public class RejectDuplicateIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = Households.resolve(request.getLastName(), request.getAddress(),
                Boolean.TRUE.equals(request.getSharesHousehold()), ownerRepository);
        String identityKey = IdentityKeys.of(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(IdentityKeys.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
