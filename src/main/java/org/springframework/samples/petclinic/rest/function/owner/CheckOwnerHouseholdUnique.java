package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerHouseholdException;

/**
 * Rejects a create-owner request that shares a household with an existing owner — the same
 * last name and the same address — with a 409. Both fields are compared case-insensitively
 * with runs of whitespace collapsed to a single space and leading/trailing whitespace
 * trimmed. The check is bypassed when the request sets {@code sharesHousehold} true.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = canonical(request.getLastName());
        String address = canonical(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(canonical(existing.getLastName()))
                    && address.equals(canonical(existing.getAddress()))) {
                throw new DuplicateOwnerHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    private static String canonical(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
