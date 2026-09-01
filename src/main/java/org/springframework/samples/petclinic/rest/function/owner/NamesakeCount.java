package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts an owner's namesakes: the other owners sharing this owner's first and last name, compared
 * case-insensitively. The owner itself is excluded by id, so for a freshly created owner this equals
 * the number of owners that existed before the create (none of that owner's namesakes are created by
 * the same request), matching the {@code namesakeCount} the create endpoint reports.
 */
public final class NamesakeCount {

    private NamesakeCount() {
    }

    public static int of(Owner owner, OwnerRepository repository) {
        int count = 0;
        for (Owner other : repository.findAll()) {
            if (!Objects.equals(other.getId(), owner.getId())
                    && equalsIgnoreCase(owner.getFirstName(), other.getFirstName())
                    && equalsIgnoreCase(owner.getLastName(), other.getLastName())) {
                count++;
            }
        }
        return count;
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
