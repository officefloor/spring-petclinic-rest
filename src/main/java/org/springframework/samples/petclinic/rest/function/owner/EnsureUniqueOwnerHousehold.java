package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * On create, rejects the owner when another owner already shares its last name and address
 * (compared case-insensitively with collapsed whitespace). Skipped when the request opts in
 * via {@code sharesHousehold=true}.
 */
public class EnsureUniqueOwnerHousehold {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String household = household(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (!Objects.equals(existing.getId(), owner.getId())
                    && household.equals(household(existing))) {
                throw new DuplicateHouseholdException(
                        "Another owner already shares this last name and address");
            }
        }
    }

    private static String household(Owner owner) {
        return normalize(owner.getLastName()) + '\n' + normalize(owner.getAddress());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
