package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;

/**
 * Rejects a create-owner request whose normalized telephone is already used by any existing
 * owner. Runs after {@link ValidateNewOwner} has normalized the request's telephone to its 10
 * digits, and before {@link BuildOwner}, so a duplicate is a 409 rather than a persisted record.
 * Each stored owner's telephone is normalized the same way (non-digits stripped) before comparing.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String telephone = normalize(request.getTelephone());
        for (Owner owner : ownerRepository.findAll()) {
            if (telephone.equals(normalize(owner.getTelephone()))) {
                throw new DuplicateOwnerTelephoneException(request.getTelephone());
            }
        }
    }

    private static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
