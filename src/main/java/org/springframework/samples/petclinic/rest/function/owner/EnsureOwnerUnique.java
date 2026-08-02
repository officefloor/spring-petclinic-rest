package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerAlreadyExistsException;

/**
 * Rejects creation of an <code>Owner</code> when it duplicates an existing owner.
 *
 * <p>Duplicate detection ignores letter case and surrounding or repeated whitespace,
 * so {@code "  john   smith "} and {@code "John Smith"} count as the same person. The
 * same normalisation applies to telephone and email so trivially different spellings
 * are still recognised as duplicates.
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerAlreadyExistsException {
        String name = normalizeName(owner);
        String telephone = normalizeTelephone(owner.getTelephone());
        String email = normalize(owner.getEmail());
        boolean hasEmail = email != null && !email.isEmpty();
        for (Owner existing : ownerRepository.findAll()) {
            if (!isDifferentOwner(existing, owner)) {
                continue;
            }
            if (name != null && name.equals(normalizeName(existing))
                    && telephone != null && telephone.equals(normalizeTelephone(existing.getTelephone()))) {
                throw new OwnerAlreadyExistsException(
                        "An owner with the same name and telephone already exists");
            }
            if (telephone != null && telephone.equals(normalizeTelephone(existing.getTelephone()))) {
                throw new OwnerAlreadyExistsException(
                        "An owner with the same telephone already exists");
            }
            if (hasEmail && email.equals(normalize(existing.getEmail()))) {
                throw new OwnerAlreadyExistsException(
                        "An owner with the same email already exists");
            }
        }
    }

    private static boolean isDifferentOwner(Owner existing, Owner candidate) {
        return existing.getId() == null || !existing.getId().equals(candidate.getId());
    }

    /** Normalised full name ("firstName lastName"), or {@code null} when both are blank. */
    private static String normalizeName(Owner owner) {
        String combined = normalize((orEmpty(owner.getFirstName()) + " " + orEmpty(owner.getLastName())));
        return (combined == null || combined.isEmpty()) ? null : combined;
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * Reduces a telephone to digits only so differently punctuated spellings of the
     * same number (e.g. {@code "(613) 555-0100"} and {@code "6135550100"}) compare
     * equal. Returns {@code null} when blank.
     */
    private static String normalizeTelephone(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : digits;
    }

    /**
     * Lower-cases and collapses whitespace so that comparisons ignore letter case and
     * surrounding or repeated whitespace.
     */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
