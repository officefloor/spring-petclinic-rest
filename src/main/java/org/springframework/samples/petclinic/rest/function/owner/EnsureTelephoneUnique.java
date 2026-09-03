package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Runs after {@link ValidateOwnerFields} has normalized the telephone to E.164 form: rejects the
 * request when any existing owner already has the same E.164 telephone. Both sides are canonicalized
 * to E.164 before comparison so differing formats do not hide a duplicate. On a match it throws
 * {@link DuplicateTelephoneException}, handled globally as 409.
 */
public class EnsureTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = canonical(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(canonical(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(request.getTelephone());
            }
        }
    }

    /**
     * The E.164 form used for comparison. The request's telephone is already E.164; an existing
     * value that predates E.164 storage is normalized on the fly, falling back to its bare digits if
     * it cannot form valid E.164.
     */
    private static String canonical(String value) {
        try {
            return OwnerTelephone.toE164(value);
        } catch (InvalidTelephoneException ex) {
            return value == null ? "" : value.replaceAll("\\D", "");
        }
    }
}
