package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create-owner request whose normalized telephone is already used by any other
 * owner, responding 409 via {@link DuplicateTelephoneException}.
 *
 * <p>Runs after {@link ValidateOwnerFields} (which has already normalized the request's
 * telephone to exactly 10 digits) and before {@link BuildOwner} saves anything. Each
 * stored owner's telephone is normalized the same way — stripping every non-digit
 * character — before comparison, so equivalent numbers written in different formats still
 * collide.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = normalize(request.getTelephone());
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
