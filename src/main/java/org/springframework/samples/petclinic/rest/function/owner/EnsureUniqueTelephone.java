package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create-owner request whose normalized telephone is already used by an existing owner,
 * responding 409 via {@link DuplicateTelephoneException}. Runs after {@link NormalizeOwnerTelephone}
 * (which guarantees the request telephone is the 10-digit normalized form) and before
 * {@link BuildOwner}, comparing against every stored owner's telephone normalized the same way
 * (all non-digit characters stripped).
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (normalize(existing.getTelephone()).equals(telephone)) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }

    private static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
