package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets {@code householdSize} to the number of members the owner's household has after this create:
 * every existing owner sharing the same household plus the owner being created. Two owners belong to
 * the same household when their last names are equal after trimming, collapsing runs of whitespace to
 * a single space, and lower-casing, and their addresses are equal in the normalized form
 * ({@link AddressNormalizer}) — the same rule {@link RejectDuplicateHousehold} and {@link HouseholdId}
 * use. Runs before the owner is saved, so {@link OwnerRepository#findAll()} sees only the owners that
 * existed before this create, which are then counted alongside this one.
 */
public class CountHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = normalize(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        int count = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
