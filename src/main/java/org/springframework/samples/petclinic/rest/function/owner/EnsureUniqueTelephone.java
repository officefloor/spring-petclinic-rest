package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create request whose (already normalized) telephone matches an existing owner's, so a
 * telephone is unique across owners. Runs after {@link NormalizeOwnerTelephone} — the request now
 * holds an E.164 value — and before {@link BuildOwner}; a match is a 409 via
 * {@link DuplicateTelephoneException}. Existing telephones are converted to E.164 the same way
 * before comparison so seed data stored in other formats still counts as a duplicate.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(E164Telephone.toE164OrNull(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
