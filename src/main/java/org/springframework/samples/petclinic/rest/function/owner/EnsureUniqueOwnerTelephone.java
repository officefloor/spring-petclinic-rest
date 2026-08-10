package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Runs after {@link NormalizeOwnerTelephone} (so the request telephone is already the E.164 value) and
 * before {@link BuildOwner}. Rejects the request when any existing owner already uses the same E.164
 * telephone by throwing {@link DuplicateTelephoneException} (handled as 409). Existing owners'
 * telephones are converted to E.164 the same way before comparison so differently-formatted values
 * still collide.
 */
public class EnsureUniqueOwnerTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(toE164(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }

    /** Best-effort E.164 for an existing owner; falls back to the raw value when it cannot form E.164. */
    private static String toE164(String telephone) {
        try {
            return TelephoneE164.toE164(telephone);
        }
        catch (InvalidTelephoneException ex) {
            return telephone;
        }
    }
}
