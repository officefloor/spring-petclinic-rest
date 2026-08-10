package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create-owner request whose normalized telephone is already used by any other owner.
 * Runs after {@link ValidateOwnerFields} (which strips non-digit characters and republishes the
 * request as a variable) and before {@link BuildOwner}. Every existing owner's telephone is
 * normalized the same way before comparison, and a match is reported as a 409 (see
 * {@link DuplicateTelephoneException}).
 */
public class CheckUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = normalize(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(request.getTelephone());
            }
        }
    }

    private static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
