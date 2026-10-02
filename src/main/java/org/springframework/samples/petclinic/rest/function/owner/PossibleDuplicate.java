package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Canonical soft-match (possible-duplicate) rule for the owner pipelines, defined once so the
 * create and fetch pipelines agree. An owner is a possible duplicate of an existing owner when it
 * is not a hard duplicate (their {@link OwnerIdentity identity keys} differ, so the create was not
 * rejected with a 409) yet they share the {@link Soundex soundex} of their last name and the same
 * postcode.
 *
 * <p>This is the behaviour change of the single-identity-key design: two owners with the same last
 * name and postcode but different telephones now derive different identity keys, so they are
 * created rather than rejected, and each is flagged as a soft match of the other.
 *
 * <p>The match compares the last name by its soundex code and the postcode by its trimmed value. A
 * possible duplicate is only possible when the owner itself has a postcode. When several existing
 * owners match, the one with the lowest id (the earliest created) is reported for a deterministic
 * result.
 */
final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    /**
     * The id of the existing owner that {@code owner} possibly duplicates, or {@code null} when it
     * is not a possible duplicate. The owner itself (matched by id) is never considered its own
     * duplicate, an existing owner carrying the identical identity key is a hard duplicate rather
     * than a soft match (so it is skipped), and only {@link ActiveOwners active owners} are scanned
     * so a soft-deleted owner is never reported as a match.
     */
    static Integer matchOf(Owner owner, OwnerRepository ownerRepository) {
        String postcode = normalizePostcode(owner.getPostcode());
        if (postcode == null) {
            return null;
        }
        String soundex = Soundex.encode(owner.getLastName());
        String key = OwnerIdentity.of(owner);
        Integer match = null;
        for (Owner existing : ActiveOwners.list(ownerRepository)) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (!soundex.equals(Soundex.encode(existing.getLastName()))) {
                continue;
            }
            if (!postcode.equals(normalizePostcode(existing.getPostcode()))) {
                continue;
            }
            if (key.equals(OwnerIdentity.of(existing))) {
                // Identical identity key: a hard duplicate, not a soft match.
                continue;
            }
            if (existing.getId() != null && (match == null || existing.getId() < match)) {
                match = existing.getId();
            }
        }
        return match;
    }

    /** Trimmed postcode, or {@code null} when absent/blank (no postcode means no soft match). */
    private static String normalizePostcode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
