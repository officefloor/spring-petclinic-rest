package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags whether a newly created owner shares a household with an owner that
 * already exists: {@code true} when, at the moment this owner is created, another
 * owner already has both the same address and the same city, otherwise
 * {@code false}. The owner being created is not yet saved, so it is never compared
 * against itself. The comparison is case-insensitive on both address and city.
 */
public class AssignOwnerSharesHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String address = owner.getAddress();
        String city = owner.getCity();
        boolean shares = ownerRepository.findAll().stream()
                .anyMatch(existing -> equalsIgnoreCase(existing.getAddress(), address)
                        && equalsIgnoreCase(existing.getCity(), city));
        owner.setSharesHousehold(shares);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) {
            return Objects.equals(a, b);
        }
        return a.equalsIgnoreCase(b);
    }
}
