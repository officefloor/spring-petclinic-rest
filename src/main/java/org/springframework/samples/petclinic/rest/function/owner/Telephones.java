package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Telephone normalisation shared across the owner pipeline.
 *
 * <p>The stored form of a telephone number is digits only: spaces, dashes and
 * parentheses accepted on input are stripped so {@code "(613) 555-0100"} and
 * {@code "6135550100"} become the same value. This lets the uniqueness check treat
 * differently punctuated spellings of the same number as duplicates.
 */
final class Telephones {

    private Telephones() {
    }

    /** Returns the digits-only form of {@code telephone}, or {@code null} when null. */
    static String digitsOnly(String telephone) {
        return telephone == null ? null : telephone.replaceAll("[^0-9]", "");
    }
}
