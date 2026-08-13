package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of existing owners that share
 * the same {@code firstName} and {@code lastName}, compared case-insensitively.
 *
 * <p>Runs after {@link BuildOwner} (which produces the {@link Owner}) and before
 * {@link SaveOwner} in the {@code POST /api/owners} pipeline, so the count it reads
 * excludes the owner being created; the value is then persisted with the new owner and
 * returned by later reads. It mutates the built {@link Owner} in place (see
 * {@code @Val} semantics).
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (equalsIgnoreCase(firstName, existing.getFirstName())
                    && equalsIgnoreCase(lastName, existing.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
