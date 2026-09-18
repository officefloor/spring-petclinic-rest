package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs in {@code POST /api/owners} after the owner entity is built and before it is saved,
 * within the create transaction. Records on the built owner how many existing owners already
 * shared its firstName and lastName (compared case-insensitively) at the moment of creation.
 * Because the new owner has not yet been persisted, {@link OwnerRepository#findAll()} returns
 * only the owners that existed before this create, so the count excludes the owner itself.
 */
public class AssignOwnerNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long count = ownerRepository.findAll().stream()
                .filter(existing -> isNamesake(owner, existing))
                .count();
        owner.setNamesakeCount((int) count);
    }

    /** Whether the two owners share the same first and last name, ignoring case. */
    private static boolean isNamesake(Owner a, Owner b) {
        return a.getFirstName().equalsIgnoreCase(b.getFirstName())
                && a.getLastName().equalsIgnoreCase(b.getLastName());
    }
}
