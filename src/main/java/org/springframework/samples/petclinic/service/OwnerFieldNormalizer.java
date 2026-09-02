package org.springframework.samples.petclinic.service;

/**
 * Shared canonicalisation of the owner fields the identity, household and duplicate rules key on.
 * Extracted so every policy normalises a given field identically instead of repeating the same
 * null-guard and regex in each class.
 */
final class OwnerFieldNormalizer {

    private OwnerFieldNormalizer() {
    }

    /** Telephone reduced to its digits ({@code null} &rarr; {@code ""}). */
    static String telephone(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }

    /** Email lower-cased ({@code null} &rarr; {@code ""}). */
    static String email(String email) {
        return email == null ? "" : email.toLowerCase();
    }

    /** Name trimmed, internal whitespace collapsed to a single space and lower-cased
     *  ({@code null} &rarr; {@code ""}). */
    static String name(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** {@code value}, or {@code ""} when {@code null}. */
    static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
