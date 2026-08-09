package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneConflictException;

/**
 * Rejects a create-owner request whose normalized telephone is already used by any other owner.
 * The telephone was normalized to ten digits by {@link ValidateOwnerFields} and published on the
 * body; this step compares it (digits only, to be robust) against every existing owner and throws
 * {@link OwnerTelephoneConflictException} (handled as 409) on a match.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerTelephoneConflictException {
        String telephone = normalize(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(normalize(existing.getTelephone()))) {
                throw new OwnerTelephoneConflictException(request.getTelephone());
            }
        }
    }

    private static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
