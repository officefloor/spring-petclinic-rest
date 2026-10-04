package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for the owner telephone normalisation rule used across the
 * {@code POST /owners} pipeline: a normalised telephone is the input with every non-digit
 * character stripped. Both {@link NormalizeOwnerTelephone} (which additionally enforces the
 * 10-digit length) and {@link CheckOwnerTelephoneUnique} (which compares stored values) must
 * agree on this definition, so it lives here rather than being re-implemented at each site.
 */
final class OwnerTelephone {

    private OwnerTelephone() {
    }

    /** Strip every non-digit character; a {@code null} input normalises to an empty string. */
    static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
