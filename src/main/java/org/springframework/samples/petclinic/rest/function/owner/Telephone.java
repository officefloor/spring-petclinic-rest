package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalises telephone numbers to digits only by stripping spaces, dashes and
 * parentheses. This is applied before an owner is stored and before the
 * telephone-uniqueness check, so {@code "(613) 555-0100"} and {@code "6135550100"}
 * are stored, returned and compared as the same number.
 */
final class Telephone {

    private Telephone() {
    }

    /**
     * @return {@code value} with spaces, dashes and parentheses removed, or
     *         {@code null} when {@code value} is {@code null}.
     */
    static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[\\s()-]", "");
    }
}
