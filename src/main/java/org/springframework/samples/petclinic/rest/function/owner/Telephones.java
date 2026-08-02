package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Telephone normalization shared by the owner request pipeline.
 *
 * <p>Stored telephone numbers are kept as digits only: spaces, dashes and
 * parentheses (and any other non-digit punctuation) are stripped. Applying this
 * before the uniqueness check means {@code "(613) 555-0100"} and {@code "6135550100"}
 * are treated as the same number.
 */
final class Telephones {

    private Telephones() {
    }

    /**
     * Reduces a telephone value to its digits, returning {@code null} unchanged.
     */
    static String digitsOnly(String telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.replaceAll("[^0-9]", "");
    }
}
