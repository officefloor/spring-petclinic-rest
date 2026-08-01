package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalisation for owner telephone numbers. The stored value keeps digits only, so formatting such
 * as spaces, dashes and parentheses is stripped. This is applied before the telephone-uniqueness
 * check so {@code "(613) 555-0100"} and {@code "6135550100"} are treated as the same number.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /**
     * Digits-only form of a telephone number, removing spaces, dashes, parentheses and any other
     * non-digit character. Returns {@code null} for a {@code null} input.
     */
    static String digitsOnly(String telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.replaceAll("\\D", "");
    }
}
