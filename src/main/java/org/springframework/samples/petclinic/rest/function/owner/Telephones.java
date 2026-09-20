package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Shared telephone normalization: strips every non-digit character so equivalent numbers
 * written with different separators compare equal. Used both to normalize a create
 * request before it is persisted and to compare it against existing owners' numbers.
 */
final class Telephones {

    private Telephones() {
    }

    /** Returns {@code telephone} reduced to its digits ("" when {@code null}). */
    static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
