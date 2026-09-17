package org.springframework.samples.petclinic.rest.validation;

/**
 * Normalizes a telephone number to its bare digits by removing every non-digit character.
 */
public final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /**
     * Strip every non-digit character from the given telephone.
     *
     * @param telephone the raw telephone, possibly {@code null} or containing formatting characters
     * @return the digits contained in {@code telephone}; an empty string when {@code telephone} is {@code null}
     */
    public static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
