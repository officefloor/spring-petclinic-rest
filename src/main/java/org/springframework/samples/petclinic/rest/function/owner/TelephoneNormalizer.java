package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single definition of "normalized telephone": every non-digit character removed. Shared by
 * {@link NormalizeOwnerTelephone} (which stores the normalized value) and {@link RequireUniqueTelephone}
 * (which compares against it), so both apply the same rule and cannot drift apart.
 *
 * <p>Kept out of the function classes themselves because OfficeFloor requires each function class to
 * expose exactly one public method.
 */
final class TelephoneNormalizer {

    static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }

    private TelephoneNormalizer() {
    }
}
