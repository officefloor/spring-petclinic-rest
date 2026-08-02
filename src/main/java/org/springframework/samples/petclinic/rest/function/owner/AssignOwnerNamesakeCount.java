package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner its namesake count: the number of other owners
 * that already share the same last name at the moment this owner is created. The
 * owner being created is not yet saved, so it is never counted among its own
 * namesakes - the first owner with a given surname gets {@code 0}, the second
 * gets {@code 1}, and so on.
 */
public class AssignOwnerNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        long namesakes = ownerRepository.findAll().stream()
                .filter(existing -> Objects.equals(existing.getLastName(), lastName))
                .count();
        owner.setNamesakeCount((int) namesakes);
    }
}
