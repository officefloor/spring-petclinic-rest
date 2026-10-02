package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Canonical soft-match (possible-duplicate) rule for the owner pipelines, defined once so the
 * create and fetch pipelines agree. An owner is a possible duplicate of an existing owner when
 * it is not a hard duplicate (that is already rejected by {@link ValidateUniqueIdentity}) but
 * shares that owner's last name and postcode while carrying a different telephone.
 *
 * <p>The match compares the last name case-insensitively with collapsed whitespace, the postcode
 * by its trimmed value and the telephone by its normalized E.164 form. A possible duplicate is
 * only possible when the owner itself has a postcode. When several existing owners match, the one
 * with the lowest id (the earliest created) is reported for a deterministic result.
 */
final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    /**
     * The id of the existing owner that {@code owner} possibly duplicates, or {@code null} when
     * it is not a possible duplicate. The owner itself (matched by id) is never considered its
     * own duplicate.
     */
    static Integer matchOf(Owner owner, OwnerRepository ownerRepository) {
        String postcode = normalizePostcode(owner.getPostcode());
        if (postcode == null) {
            return null;
        }
        String lastName = normalizeName(owner.getLastName());
        String telephone = OwnerTelephone.normalize(owner.getTelephone());
        Integer match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (!lastName.equals(normalizeName(existing.getLastName()))) {
                continue;
            }
            if (!postcode.equals(normalizePostcode(existing.getPostcode()))) {
                continue;
            }
            if (sameTelephone(telephone, existing.getTelephone())) {
                continue;
            }
            if (existing.getId() != null && (match == null || existing.getId() < match)) {
                match = existing.getId();
            }
        }
        return match;
    }

    /** Trim, collapse whitespace and lower-case a name; {@code ""} for a {@code null} input. */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Trimmed postcode, or {@code null} when absent/blank (no postcode means no soft match). */
    private static String normalizePostcode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /** Whether two telephones are the same once normalized to E.164 form. */
    private static boolean sameTelephone(String normalized, String other) {
        String otherNormalized = OwnerTelephone.normalize(other);
        if (normalized == null) {
            return otherNormalized == null;
        }
        return normalized.equals(otherNormalized);
    }
}
