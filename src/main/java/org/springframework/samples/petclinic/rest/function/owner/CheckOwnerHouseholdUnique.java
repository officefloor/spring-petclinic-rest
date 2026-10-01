package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose last name and address, normalized, already belong to another owner.
 *
 * <p>Runs after {@link ValidateOwnerFields} and before {@link BuildOwner}. Both the request and
 * every stored owner's last name and address are reduced through {@link OwnerHousehold#normalize}
 * (case-insensitive, whitespace collapsed) before comparison, so differently cased or spaced values
 * that mean the same thing still collide. A match throws {@link OwnerHouseholdDuplicateException},
 * handled as a 409 Conflict — unless the request opted in with {@code sharesHousehold: true}, which
 * permits owners who genuinely share a household.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerHouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = OwnerHousehold.normalize(request.getLastName());
        String address = OwnerHousehold.normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(OwnerHousehold.normalize(existing.getLastName()))
                    && address.equals(OwnerHousehold.normalize(existing.getAddress()))) {
                throw new OwnerHouseholdDuplicateException(request.getLastName(),
                        request.getAddress());
            }
        }
    }
}
