package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Shared matching helpers for the {@code POST /api/owners} soft-matching steps that scan
 * {@code ownerRepository.findAll()} comparing owner name/contact fields (e.g. {@link CountNamesakes},
 * {@link CheckPossibleDuplicate}). Keeps the matching semantics defined in one place.
 */
final class OwnerMatch {

    private OwnerMatch() {
    }

    /** Null-safe, case-insensitive string equality. */
    static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
