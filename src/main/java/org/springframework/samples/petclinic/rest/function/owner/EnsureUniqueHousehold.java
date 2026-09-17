package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Create-owner step: rejects the request with a 409 when another owner already shares the
 * same household — the same last name and the same address, compared case-insensitively
 * with runs of whitespace collapsed to a single space. Skipped when the request opts in
 * with {@code sharesHousehold: true}. Runs after {@link ValidateOwnerFields} (so the
 * required fields are present) and before {@link BuildOwner}.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        for (Owner owner : ownerRepository.findAll()) {
            if (lastName.equals(normalize(owner.getLastName()))
                    && address.equals(normalize(owner.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Trimmed and lower-cased, with internal whitespace runs collapsed to a single space. */
    private static String normalize(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
