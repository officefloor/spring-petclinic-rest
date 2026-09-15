package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Shared telephone normalization for the create-owner pipeline: strip every non-digit
 * character. Used both by {@link NormalizeOwnerTelephone} (which then requires exactly
 * ten digits) and by {@link EnsureUniqueTelephone} (which normalizes each stored
 * telephone before comparing), so the two steps agree on what "normalized" means.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
