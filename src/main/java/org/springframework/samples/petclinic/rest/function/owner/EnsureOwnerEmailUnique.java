package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creation of an owner when an email address is provided and another owner
 * already uses the same email. Email is optional; when absent no check is applied.
 */
public class EnsureOwnerEmailUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional
        }
        String normalizedEmail = normalize(email);
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(owner, existing)) {
                continue;
            }
            if (Objects.equals(normalizedEmail, normalize(existing.getEmail()))) {
                throw new DuplicateOwnerException(
                        "An owner with the same email already exists");
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
