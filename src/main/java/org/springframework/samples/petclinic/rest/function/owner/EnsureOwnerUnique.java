package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerConflictException;
import org.springframework.util.StringUtils;

/**
 * Rejects creating an owner that clashes with an existing one - with a 409 via
 * {@link OwnerConflictException}. An owner clashes when its telephone number is
 * already used by another owner, or (when an email address is provided) when that
 * email is already used by another owner.
 *
 * <p>Duplicate detection is insensitive to letter case and to surrounding or
 * repeated whitespace: values are {@link #normalize(String) normalized} before
 * comparison, so {@code "  john   smith "} and {@code "John Smith"} are treated as
 * the same value.
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws OwnerConflictException {
        String telephone = normalize(owner.getTelephone());
        String email = normalize(owner.getEmail());
        boolean hasEmail = StringUtils.hasText(email);
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(normalize(existing.getTelephone()), telephone)) {
                throw new OwnerConflictException(
                        "An owner with the same telephone already exists");
            }
            if (hasEmail && Objects.equals(normalize(existing.getEmail()), email)) {
                throw new OwnerConflictException(
                        "An owner with the same email already exists");
            }
        }
    }

    /**
     * Normalizes a value for case- and whitespace-insensitive comparison: trims
     * surrounding whitespace, collapses each run of internal whitespace to a single
     * space, and lower-cases the result. {@code null} maps to {@code null}.
     */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
