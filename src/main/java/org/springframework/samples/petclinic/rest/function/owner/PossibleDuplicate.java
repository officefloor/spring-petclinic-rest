package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Finds the existing owner this owner is a possible (soft) duplicate of: another owner sharing this
 * owner's last name (case-insensitive) and postcode but with a different telephone. A shared last
 * name and postcode with a matching telephone would be a hard duplicate (rejected at create, see
 * {@link RejectDuplicateOwnerIdentity}), so a differing telephone is exactly the soft-match case.
 * Owners without a postcode never soft-match. Returns the matching owner's id (the lowest when
 * several match), or {@code null} when there is no possible duplicate.
 */
public final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    public static Integer of(Owner owner, OwnerRepository repository) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        Integer match = null;
        for (Owner other : repository.findAll()) {
            if (!Objects.equals(other.getId(), owner.getId()) && postcode.equals(other.getPostcode())
                    && equalsIgnoreCase(owner.getLastName(), other.getLastName())
                    && !Objects.equals(owner.getTelephone(), other.getTelephone())
                    && (match == null || other.getId() < match)) {
                match = other.getId();
            }
        }
        return match;
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
