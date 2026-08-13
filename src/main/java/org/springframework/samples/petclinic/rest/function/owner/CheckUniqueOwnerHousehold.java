package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs on {@code POST /api/owners} before {@link BuildOwner} (so no owner is created on conflict):
 * rejects the request with 409 via {@link DuplicateHouseholdException} when another owner already
 * has the same last name and the same address, compared case-insensitively with collapsed
 * whitespace. The check is skipped when the request opts in with {@code sharesHousehold} true,
 * letting several owners share one household.
 */
public class CheckUniqueOwnerHousehold {

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

    /** Lower-case and collapse runs of whitespace so comparison is case- and whitespace-insensitive. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
