package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft-match duplicate. A new owner that is not a hard duplicate (that case is already
 * rejected by {@link EnsureUniqueIdentity}) but that shares an existing owner's last name and
 * postcode while carrying a different telephone is still created, with {@code possibleDuplicate}
 * set true and {@code possibleDuplicateOf} set to the matching owner's id. When no such owner
 * exists {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is left absent.
 *
 * <p>Runs before {@link SaveOwner} so the new owner has not been persisted yet and is not compared
 * against itself. Last name is compared case-insensitively; the postcode must be present and equal.
 * The earliest matching owner (lowest id) is chosen for stability.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        if (postcode == null || postcode.isBlank()) {
            owner.setPossibleDuplicate(Boolean.FALSE);
            return;
        }
        Owner match = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> equalsIgnoreCase(existing.getLastName(), lastName)
                        && postcode.equals(existing.getPostcode())
                        && !equalsIgnoreCase(existing.getTelephone(), telephone))
                .min(java.util.Comparator.comparing(Owner::getId))
                .orElse(null);
        if (match != null) {
            owner.setPossibleDuplicate(Boolean.TRUE);
            owner.setPossibleDuplicateOf(match.getId());
        }
        else {
            owner.setPossibleDuplicate(Boolean.FALSE);
        }
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
