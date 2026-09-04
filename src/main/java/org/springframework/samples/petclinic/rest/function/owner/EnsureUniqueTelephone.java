package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create request whose (already normalized) telephone matches an existing owner's, so a
 * telephone is unique across owners. Runs after {@link NormalizeOwnerTelephone} — the request now
 * holds a 10-digit value — and before {@link BuildOwner}; a match is a 409 via
 * {@link DuplicateTelephoneException}. Existing telephones are normalized the same way before
 * comparison so seed data stored in other formats still counts as a duplicate.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
