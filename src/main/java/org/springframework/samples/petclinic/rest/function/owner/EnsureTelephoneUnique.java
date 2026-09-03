package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Runs after {@link ValidateOwnerFields} has normalized the telephone to its bare digits: rejects
 * the request when any existing owner already has the same normalized telephone. Comparison strips
 * every non-digit character from both sides so differing punctuation does not hide a duplicate. On a
 * match it throws {@link DuplicateTelephoneException}, handled globally as 409.
 */
public class EnsureTelephoneUnique {

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
