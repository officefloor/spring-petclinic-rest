package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Create-owner step: rejects the request with a 409 when its telephone is already used by
 * any existing owner, comparing telephones in canonical E.164 form (see
 * {@link E164Telephone}). Runs after {@link NormalizeOwnerTelephone} (so the request
 * already holds the E.164 value) and before {@link BuildOwner}.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner owner : ownerRepository.findAll()) {
            if (telephone.equals(canonical(owner.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }

    /** An existing telephone in E.164 form, or {@code null} when it cannot be normalized
     *  (such a stored value never matches a valid incoming number). */
    private static String canonical(String stored) {
        try {
            return E164Telephone.normalize(stored);
        }
        catch (InvalidTelephoneException ex) {
            return null;
        }
    }
}
