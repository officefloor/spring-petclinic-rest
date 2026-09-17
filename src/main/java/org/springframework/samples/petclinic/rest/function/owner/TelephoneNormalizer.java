package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Canonical telephone normalization shared by the create pipeline: {@link NormalizeTelephone} stores
 * the digits-only form and {@link EnsureUniqueTelephone} compares by it. Not a pipeline step, so it
 * is free to expose plain helpers.
 */
public final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /** Strips every non-digit character, yielding the form two telephones are compared by. */
    public static String digitsOnly(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
