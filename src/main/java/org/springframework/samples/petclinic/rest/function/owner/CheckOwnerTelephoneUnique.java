package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create-owner request whose normalized telephone is already used by any other owner.
 * Runs after {@link ValidateOwnerFields} has normalized the body's telephone to its digits, and
 * compares it against every existing owner's telephone (normalized the same way). Throws
 * {@link DuplicateTelephoneException} (handled as 409) on a collision.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = normalize(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(request.getTelephone());
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
