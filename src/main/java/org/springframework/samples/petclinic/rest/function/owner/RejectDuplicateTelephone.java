package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose normalized telephone is already used by any other owner.
 * Runs after {@link NormalizeTelephone} (so the request telephone is already reduced to its digits)
 * and before the owner is built and saved. Each existing owner's telephone is normalized the same way
 * before comparison, so numbers stored in any format are matched. A collision is rejected via
 * {@link DuplicateTelephoneException}, which the global handler turns into a 409.
 */
public class RejectDuplicateTelephone {

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
