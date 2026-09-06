package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Single home for owner telephone normalization. Both the create pipeline's
 * {@link BuildOwner} (which stores the canonical form) and
 * {@link CheckOwnerTelephoneUnique} (which detects duplicates) go through here, so the
 * stored form and the duplicate-comparison stay defined in one place.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /**
     * Canonical form to store for a supplied telephone. Strips every non-digit character
     * and requires exactly 10 digits, else rejects the request with a 400.
     */
    static String normalize(String telephone) throws InvalidTelephoneException {
        String digits = digitsOf(telephone);
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(telephone);
        }
        return digits;
    }

    /**
     * Comparison key used to detect duplicate telephones. Lenient (never throws) so that
     * whatever is already stored on existing owners still compares.
     */
    static String comparisonKey(String telephone) {
        return digitsOf(telephone);
    }

    private static String digitsOf(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
