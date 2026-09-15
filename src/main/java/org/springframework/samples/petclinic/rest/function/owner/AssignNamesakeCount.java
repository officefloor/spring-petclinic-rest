package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records on a newly built owner how many existing owners already share its first and
 * last name, compared case-insensitively. Runs after {@link BuildOwner} and before
 * {@link SaveOwner} — so the new owner itself is not yet persisted and thus not counted —
 * and mutates the built owner in place, so the count is stored and returned with it.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String key = nameKey(owner.getFirstName(), owner.getLastName());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (key.equals(nameKey(existing.getFirstName(), existing.getLastName()))) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    /** The first and last name folded to a single case-insensitive key. */
    private static String nameKey(String firstName, String lastName) {
        return fold(firstName) + '\n' + fold(lastName);
    }

    private static String fold(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
