package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Step of {@code POST /api/owners} that rejects a create whose telephone is already used by an
 * existing owner, throwing {@link DuplicateTelephoneException} (handled as 409 Conflict). Runs after
 * {@link RequireOwnerFields} has normalized the telephone to E.164 form and published the body, and
 * before {@link BuildOwner}/{@link SaveOwner} persist the new owner. Duplicates are detected by
 * comparing the canonical E.164 values.
 */
public class RequireUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone(); // already E.164 (set by RequireOwnerFields)
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(
                        "Telephone " + telephone + " is already used by another owner");
            }
        }
    }

    private static String normalize(String telephone) {
        try {
            return OwnerTelephone.toE164(telephone);
        }
        catch (InvalidTelephoneException ex) {
            return telephone == null ? "" : telephone;
        }
    }
}
