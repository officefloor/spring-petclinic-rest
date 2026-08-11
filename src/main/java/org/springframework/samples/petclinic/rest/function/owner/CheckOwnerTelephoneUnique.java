package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create-owner request whose normalized telephone already belongs to another
 * owner. Runs after {@link NormalizeOwnerTelephone} (so {@code request.getTelephone()} is
 * the canonical ten-digit form) and before {@link SaveOwner}. Every stored owner's
 * telephone is reduced to digits the same way before comparison, so differently formatted
 * spellings of the same number still collide. On a match raises
 * {@link DuplicateTelephoneException} (409).
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(digits(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }

    private static String digits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
