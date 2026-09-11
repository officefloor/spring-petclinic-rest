package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneInUseException;

/**
 * Rejects a create request whose normalized telephone is already used by any other owner.
 * Runs after {@link ValidateOwnerFields} has normalized the telephone to digits, and
 * before {@link BuildOwner}, so a duplicate is a 409 rather than a persisted record.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerTelephoneInUseException {
        String telephone = normalize(request.getTelephone());
        if (telephone == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(normalize(existing.getTelephone()))) {
                throw new OwnerTelephoneInUseException(request.getTelephone());
            }
        }
    }

    private static String normalize(String telephone) {
        if (telephone == null) {
            return null;
        }
        String digits = telephone.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }
}
