package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Single source of truth for what a "normalized" owner name (first or last) is for
 * exact comparison: trimmed and lower-cased, so name matches ignore surrounding whitespace
 * and casing. Used by {@link CountNamesakes} to count owners sharing a first and last name.
 * A null name normalizes to the empty string.
 *
 * <p>Phonetic last-name matching is a separate concern and lives elsewhere: the Soundex
 * key in {@link org.springframework.samples.petclinic.model.Owner#soundex(String)}, used by
 * the identity key and by {@link FlagPossibleDuplicate}'s soft match.
 */
final class OwnerNames {

    private OwnerNames() {
    }

    static String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}
