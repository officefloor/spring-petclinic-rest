package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalises telephone numbers to digits only, discarding spaces, dashes, parentheses and any
 * other punctuation. Applied when an owner is built or updated so the stored (and therefore
 * returned) value is digits only, and so the telephone-uniqueness check treats
 * {@code "(613) 555-0100"} and {@code "6135550100"} as the same number.
 */
final class Telephones {

    private Telephones() {
    }

    /**
     * Returns the given telephone with every non-digit character removed, or {@code null} when
     * the value is {@code null}.
     */
    static String digitsOnly(String value) {
        return value == null ? null : value.replaceAll("[^0-9]", "");
    }
}
