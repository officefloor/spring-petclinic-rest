package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerHouseholdDuplicateException;

/**
 * Rejects the create when another owner already shares this owner's household: the same
 * {@code lastName} and {@code address}, compared case-insensitively with runs of whitespace
 * collapsed to a single space. A match is rejected 409 via
 * {@link OwnerHouseholdDuplicateException}.
 *
 * <p>Bypassed when the request sets {@code sharesHousehold} true — the caller has confirmed
 * the two owners knowingly live together.
 */
public class RequireUniqueOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerHouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (normalize(existing.getLastName()).equals(lastName)
                    && normalize(existing.getAddress()).equals(address)) {
                throw new OwnerHouseholdDuplicateException(request.getLastName(),
                        request.getAddress());
            }
        }
    }

    /** Lower-case, trimmed, with internal whitespace runs collapsed to a single space. */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
