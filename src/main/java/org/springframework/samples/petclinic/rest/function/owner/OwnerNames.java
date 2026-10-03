package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Single source of truth for what a "normalized" owner name (first or last) is for
 * comparison: trimmed and lower-cased, so name matches ignore surrounding whitespace and
 * casing. Shared by the steps that compare names across owners ({@link CountNamesakes}
 * counts namesakes, {@link FlagPossibleDuplicate} matches last name) so the rule cannot
 * drift apart between them. A null name normalizes to the empty string.
 */
final class OwnerNames {

    private OwnerNames() {
    }

    static String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}
