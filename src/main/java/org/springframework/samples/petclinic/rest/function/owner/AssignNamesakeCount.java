package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        // The new owner is not yet persisted, so every stored owner with the same
        // last name is a distinct namesake at this moment.
        long namesakes = ownerRepository.findAll().stream()
                .filter(existing -> lastName != null && lastName.equals(existing.getLastName()))
                .count();
        owner.setNamesakeCount((int) namesakes);
    }
}
