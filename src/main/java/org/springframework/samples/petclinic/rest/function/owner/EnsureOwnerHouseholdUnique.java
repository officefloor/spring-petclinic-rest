package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create when another owner shares this owner's last name and address
 * (compared case-insensitively with collapsed whitespace), unless the request opted in
 * with {@code sharesHousehold}.
 */
public class EnsureOwnerHouseholdUnique {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String lastName = key(owner.getLastName());
        String address = key(owner.getAddress());
        boolean clash = ownerRepository.findAll().stream()
                .anyMatch(other -> key(other.getLastName()).equals(lastName)
                        && key(other.getAddress()).equals(address));
        if (clash) {
            throw new DuplicateHouseholdException(
                    "An owner with the same last name and address already exists");
        }
    }

    private static String key(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
