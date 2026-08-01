package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalises telephone numbers to digits only, stripping spaces, dashes and
 * parentheses (and any other non-digit formatting). This is applied before an owner is
 * stored and before the telephone-uniqueness check, so {@code "(613) 555-0100"} and
 * {@code "6135550100"} are treated as the same number.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /**
     * Returns the telephone with all non-digit characters removed. {@code null} stays
     * {@code null}.
     */
    static String digitsOnly(String telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.replaceAll("\\D", "");
    }
}
