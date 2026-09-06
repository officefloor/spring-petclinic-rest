package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many existing owners (before this create) share the new owner's firstName and
 * lastName, compared case-insensitively. Runs after {@link BuildOwner} and before the owner is
 * saved, so the count reflects only pre-existing owners. Stamps the value on the owner via
 * {@link Owner#setNamesakeCount(Integer)}; it is returned as {@code namesakeCount}.
 */
public class CountOwnerNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = key(owner.getFirstName());
        String lastName = key(owner.getLastName());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // the owner being created is not its own namesake
            }
            if (firstName.equals(key(existing.getFirstName()))
                    && lastName.equals(key(existing.getLastName()))) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static String key(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
