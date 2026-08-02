package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Telephone-number normalization shared by the owner pipeline: a telephone is
 * stored and compared as digits only, so formatting (spaces, dashes and
 * parentheses) is insignificant and {@code "(613) 555-0100"} and
 * {@code "6135550100"} are treated as the same number.
 */
final class OwnerTelephones {

    private OwnerTelephones() {
    }

    /**
     * Strips every non-digit character, leaving digits only. {@code null} maps to
     * {@code null}.
     */
    static String digitsOnly(String telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.replaceAll("\\D", "");
    }
}
