package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerHouseholdDuplicateException;

/**
 * Runs after {@link ValidateOwner} and before {@link BuildOwner}. Rejects the create with a 409 via
 * {@link OwnerHouseholdDuplicateException} when another owner already has the same lastName and the
 * same address. Both fields are compared case-insensitively with surrounding whitespace stripped and
 * internal whitespace runs collapsed to a single space.
 *
 * <p>The request may opt out of this rule by setting {@code sharesHousehold} true, which permits
 * creating a second owner at the same household (same name and address).
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerHouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // opted in to sharing a household; skip the duplicate check
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                throw new OwnerHouseholdDuplicateException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Lower-cases and collapses whitespace so comparison ignores case and incidental spacing. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
