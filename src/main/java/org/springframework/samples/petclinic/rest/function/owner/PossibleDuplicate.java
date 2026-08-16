package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Detects a <em>soft</em> duplicate: an existing owner whose {@code lastName} has the same Soundex
 * code as this owner's and shares this owner's {@code postcode}, but whose identity key differs.
 *
 * <p>Such an owner is deliberately still created — unlike a hard duplicate, whose whole identity key
 * matches an existing owner's and is rejected with 409 (see {@link CheckIdentityUnique}). Because the
 * telephone is part of the identity key, two owners with the same last name and postcode but different
 * telephones have different keys, so the second is a soft match rather than a rejected duplicate.
 *
 * <p>The response surfaces the match through {@code possibleDuplicate} / {@code possibleDuplicateOf}.
 * The earliest (lowest id) matching owner is reported so the result is stable.
 */
public final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    /**
     * The id of an existing owner whose last name shares {@code owner}'s Soundex code and whose
     * postcode matches, but whose identity key differs, or {@code null} when there is no such match
     * (including when the owner has no postcode).
     */
    public static Integer matchFor(Owner owner, OwnerRepository ownerRepository) {
        String postcode = normalizePostcode(owner.getPostcode());
        if (postcode == null) {
            return null;
        }
        String soundex = OwnerIdentity.soundex(owner.getLastName());
        String identityKey = OwnerIdentity.of(owner);
        Integer match = null;
        for (Owner other : ownerRepository.findAll()) {
            if (other.getId() == null || other.getId().equals(owner.getId())) {
                continue;
            }
            if (!postcode.equals(normalizePostcode(other.getPostcode()))) {
                continue;
            }
            if (!soundex.equals(OwnerIdentity.soundex(other.getLastName()))) {
                continue;
            }
            if (identityKey.equals(OwnerIdentity.of(other))) {
                continue; // same identity key would be a hard duplicate, not a soft match
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
}
