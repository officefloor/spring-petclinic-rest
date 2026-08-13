package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request that shares a household with an existing owner — the
 * same {@code lastName} (compared case-insensitively with collapsed whitespace) and the
 * same {@code address} in its normalized form (see {@link AddressNormalizer}) —
 * responding 409 via {@link DuplicateHouseholdException}.
 *
 * <p>The check is skipped when the request opts in with {@code sharesHousehold: true},
 * allowing multiple owners to live at the same household.
 *
 * <p>Runs after {@link ValidateOwnerFields} (which publishes the request body) and
 * before {@link BuildOwner} saves anything.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalizeAddress(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalizeAddress(existing.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Lower-cases and collapses runs of whitespace to a single space, trimming the ends. */
    private static String normalize(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase();
    }

    /** The canonical address form (see {@link AddressNormalizer}); never {@code null}. */
    private static String normalizeAddress(String value) {
        String normalized = AddressNormalizer.normalize(value);
        return normalized == null ? "" : normalized;
    }
}
