package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a new owner who belongs to the same household as an existing owner — same last name and same
 * address — so the request is answered with a 409 Conflict rather than persisting a duplicate household
 * member. Last name and address are compared case-insensitively with surrounding and internal whitespace
 * collapsed, so "Smith" / " smith " and "10  Main St" / "10 Main St" collide.
 *
 * <p>The caller can opt in to sharing a household by setting {@code sharesHousehold} true on the request,
 * which skips this check (e.g. a spouse or flatmate registering under the same roof). Runs after
 * {@link CheckTelephoneUnique} and before {@link BuildOwner}.
 */
public class CheckHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Lower-case (locale-independent) with leading/trailing and repeated internal whitespace collapsed. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
