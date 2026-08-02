package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner whose telephone number is already used by any other owner —
 * by throwing {@link DuplicateOwnerException} (409).
 *
 * <p>Duplicate detection is insensitive to letter case and to surrounding or repeated
 * whitespace: {@code "  john   smith "} and {@code "John Smith"} are treated as the same
 * value (see {@link #normalize(String)}).
 */
public class CheckDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        String telephone = normalize(owner.getTelephone());
        String email = normalize(owner.getEmail());
        boolean hasEmail = !email.isEmpty();
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(existing.getId(), owner.getId())) {
                continue;
            }
            if (Objects.equals(normalize(existing.getTelephone()), telephone)) {
                throw new DuplicateOwnerException(
                        "An owner with the same telephone already exists");
            }
            if (hasEmail && Objects.equals(normalize(existing.getEmail()), email)) {
                throw new DuplicateOwnerException(
                        "An owner with the same email already exists");
            }
        }
    }

    /**
     * Normalizes a value for duplicate comparison: {@code null} becomes empty, letter
     * case is folded to lower case, surrounding whitespace is trimmed and any run of
     * internal whitespace is collapsed to a single space.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
