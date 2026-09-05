package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of existing owners that share this
 * owner's {@code firstName} and {@code lastName}, compared case-insensitively.
 *
 * <p>Runs after {@link BuildOwner} (so the Owner exists) but before {@link SaveOwner} (so the
 * owner being created is not itself counted), mutating the owner in place so the persisted
 * value — and every later read — carries the count as it stood before this create.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = normalize(owner.getFirstName());
        String lastName = normalize(owner.getLastName());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (normalize(existing.getFirstName()).equals(firstName)
                    && normalize(existing.getLastName()).equals(lastName)) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
