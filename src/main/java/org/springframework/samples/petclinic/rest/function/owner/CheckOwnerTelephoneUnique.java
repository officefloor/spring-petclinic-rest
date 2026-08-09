package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneConflictException;

/**
 * Rejects a create-owner request whose telephone is already used by any other owner.
 * The telephone was normalized to E.164 form by {@link ValidateOwnerFields} and published on the
 * body; this step compares that E.164 value against the E.164 form of every existing owner's
 * telephone and throws {@link OwnerTelephoneConflictException} (handled as 409) on a match.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerTelephoneConflictException {
        String telephone = normalize(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone != null && telephone.equals(normalize(existing.getTelephone()))) {
                throw new OwnerTelephoneConflictException(request.getTelephone());
            }
        }
    }

    /** Best-effort E.164 form for comparison; falls back to the raw value when unparseable. */
    private static String normalize(String telephone) {
        try {
            return OwnerTelephone.toE164(telephone);
        }
        catch (Exception ex) {
            return telephone;
        }
    }
}
