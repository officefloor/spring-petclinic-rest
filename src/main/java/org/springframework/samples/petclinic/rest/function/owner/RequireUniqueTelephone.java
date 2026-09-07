package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Step of {@code POST /api/owners} that rejects a create whose normalized telephone is already used
 * by an existing owner, throwing {@link DuplicateTelephoneException} (handled as 409 Conflict).
 * Runs after {@link RequireOwnerFields} has normalized the telephone to ten digits and published
 * the body, and before {@link BuildOwner}/{@link SaveOwner} persist the new owner.
 */
public class RequireUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = normalize(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(
                        "Telephone " + telephone + " is already used by another owner");
            }
        }
    }

    private static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
