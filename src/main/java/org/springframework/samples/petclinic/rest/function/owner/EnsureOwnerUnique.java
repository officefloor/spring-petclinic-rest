package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creation of an owner when another owner already uses the same telephone
 * number. Telephone must be unique across all owners.
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        String telephone = normalize(owner.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(owner, existing)) {
                continue;
            }
            if (Objects.equals(telephone, normalize(existing.getTelephone()))) {
                throw new DuplicateOwnerException(
                        "An owner with the same telephone already exists");
            }
        }
    }

    private static boolean isSameOwner(Owner a, Owner b) {
        return a.getId() != null && Objects.equals(a.getId(), b.getId());
    }

    /**
     * Normalizes a value for duplicate comparison: letter case is ignored and
     * surrounding or repeated whitespace is collapsed, so {@code "  john   smith "}
     * and {@code "John Smith"} compare equal.
     */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
