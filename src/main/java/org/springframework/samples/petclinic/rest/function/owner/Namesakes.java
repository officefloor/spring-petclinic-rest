package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts the owners that already existed before {@code owner} (created earlier, hence a
 * lower id) sharing its first and last name, compared case-insensitively. This is the
 * owner's namesake count at the moment it was created.
 */
public final class Namesakes {

    private Namesakes() {
    }

    public static int countBefore(Owner owner, OwnerRepository ownerRepository) {
        Integer id = owner.getId();
        if (id == null) {
            return 0;
        }
        return (int) ownerRepository.findAll().stream()
                .filter(other -> other.getId() != null && other.getId() < id)
                .filter(other -> other.getLastName().equalsIgnoreCase(owner.getLastName()))
                .filter(other -> other.getFirstName().equalsIgnoreCase(owner.getFirstName()))
                .count();
    }
}
