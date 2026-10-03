package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check on create: rejects a new owner only when its whole
 * identity key equals an existing owner's. The identity key is
 * {@code normalizedTelephone + "|" + (email or empty) + "|" + (householdId or empty)}
 * (see {@link Owner#identityKey}), so the former separate telephone, email and household
 * checks are now expressed through this one key.
 *
 * <p>The request already carries the E.164 telephone (from {@link NormalizeOwnerTelephone})
 * and the normalized address (from {@link NormalizeOwnerAddress}); the email is
 * canonicalized here via {@link OwnerEmails} to the form {@link BuildOwner} will store,
 * and the household id is the one {@link AssignHousehold} would assign when the request
 * opts into a shared household (otherwise empty). Because the telephone is part of the
 * key, two members of the same household with different telephones have distinct keys and
 * are both allowed; only an exact full-key match is a 409 via
 * {@link DuplicateIdentityException}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = Boolean.TRUE.equals(request.getSharesHousehold())
                ? Households.householdId(request.getLastName(), request.getAddress())
                : null;
        String identityKey = Owner.identityKey(OwnerTelephones.toE164(request.getTelephone()),
                OwnerEmails.normalize(request.getEmail()), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
