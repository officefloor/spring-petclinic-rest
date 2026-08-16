package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Detects a <em>soft</em> duplicate: an existing owner that shares this owner's {@code lastName}
 * (compared case-insensitively) and {@code postcode} but has a <em>different</em> telephone.
 *
 * <p>Such an owner is deliberately still created — unlike a hard duplicate, whose whole identity key
 * matches an existing owner's and is rejected with 409 (see {@link CheckIdentityUnique}). Because the
 * telephone differs, the identity keys differ too, so a soft match is never also a hard duplicate.
 *
 * <p>The response surfaces the match through {@code possibleDuplicate} / {@code possibleDuplicateOf}.
 * The earliest (lowest id) matching owner is reported so the result is stable.
 */
public final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    /**
     * The id of an existing owner sharing {@code owner}'s last name and postcode but with a
     * different telephone, or {@code null} when there is no such match (including when the owner has
     * no postcode).
     */
    public static Integer matchFor(Owner owner, OwnerRepository ownerRepository) {
        String postcode = normalizePostcode(owner.getPostcode());
        if (postcode == null) {
            return null;
        }
        String lastName = normalizeLastName(owner.getLastName());
        String telephone = normalizeTelephone(owner.getTelephone());
        Integer match = null;
        for (Owner other : ownerRepository.findAll()) {
            if (other.getId() == null || other.getId().equals(owner.getId())) {
                continue;
            }
            if (!postcode.equals(normalizePostcode(other.getPostcode()))) {
                continue;
            }
            if (!lastName.equals(normalizeLastName(other.getLastName()))) {
                continue;
            }
            if (telephone.equals(normalizeTelephone(other.getTelephone()))) {
                continue; // same telephone is not a soft match (and would be a hard duplicate)
            }
            if (match == null || other.getId() < match) {
                match = other.getId();
            }
        }
        return match;
    }

    private static String normalizePostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        String trimmed = postcode.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeLastName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String normalizeTelephone(String telephone) {
        String e164 = TelephoneE164.normalize(telephone);
        return e164 == null ? "" : e164;
    }
}
