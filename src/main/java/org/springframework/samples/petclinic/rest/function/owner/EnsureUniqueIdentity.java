package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.util.IdentityKey;

/**
 * Rejects a create request whose whole {@link IdentityKey} — normalized telephone, email
 * and household id — equals an existing owner's, as a 409 via
 * {@link DuplicateIdentityException}. This is the single duplicate check: because the
 * telephone is part of the key, two members of the same household with different
 * telephones have different keys and are both allowed; only an exact full-key match is a
 * duplicate.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} (so the
 * request's telephone and email are canonical) and before {@link BuildOwner}, so a
 * collision is caught before any owner is built or saved. The candidate's household id is
 * resolved the same way {@link AssignHousehold} would assign it, so an owner that would
 * join an existing household is compared with that household's id.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = Households.resolveHouseholdId(ownerRepository, request);
        String identityKey = IdentityKey.of(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(IdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
