package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Telephone normalization shared by the create-owner pipeline: keeps only the digit
 * characters of a telephone, so values are compared and stored in one canonical form.
 */
public final class OwnerTelephone {

    private OwnerTelephone() {
    }

    /** The digits of {@code telephone} with every other character stripped ("" when null). */
    public static String digits(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
