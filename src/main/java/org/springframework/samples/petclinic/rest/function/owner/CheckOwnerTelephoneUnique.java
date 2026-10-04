package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time uniqueness guard for {@code POST /owners}: rejects a request whose telephone is
 * already used by an existing owner, with a 409 via {@link DuplicateTelephoneException}.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} so the request telephone is the stored 10-digit
 * form, and compares against each existing owner's telephone normalised the same way (strip every
 * non-digit) so the check is independent of how existing values happen to be formatted. Runs
 * before {@link BuildOwner}.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (OwnerTelephone.normalize(existing.getTelephone()).equals(telephone)) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
