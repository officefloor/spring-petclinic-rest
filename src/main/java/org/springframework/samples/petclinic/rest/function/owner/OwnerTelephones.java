package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for the owner "normalized telephone" rule: strip every non-digit
 * character. Both {@link NormalizeOwnerTelephone} (which stores the normalized value) and
 * {@link RejectDuplicateTelephone} (which compares against it) go through here, so the two
 * can never drift apart.
 */
final class OwnerTelephones {

    private OwnerTelephones() {
    }

    /** Returns {@code telephone} with every non-digit character removed ("" when null). */
    static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
