package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners}: records the new owner's {@code namesakeCount} — the
 * number of existing owners that already share the same first and last name (compared
 * case-insensitively, see {@link Namesake}). It runs on the built owner before
 * {@link SaveOwner} persists it, so the new owner is not yet counted among its own
 * namesakes. The value is stored with the row and returned unchanged on later reads.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (Namesake.matches(existing, owner.getFirstName(), owner.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }
}
