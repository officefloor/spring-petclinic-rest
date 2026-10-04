package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time uniqueness guard for {@code POST /owners}: rejects a request whose telephone is
 * already used by an existing owner, with a 409 via {@link DuplicateTelephoneException}.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} so the request telephone is the stored E.164
 * form, and compares against each existing owner's telephone canonicalised the same way
 * ({@link OwnerTelephone#toE164(String)}) so the check is independent of how existing values
 * happen to be formatted. Runs before {@link BuildOwner}.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(OwnerTelephone.toE164(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
