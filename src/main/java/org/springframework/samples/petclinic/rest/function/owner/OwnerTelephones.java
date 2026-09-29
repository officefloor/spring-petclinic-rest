package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single definition of how an owner telephone is normalized: strip every non-digit
 * character. Both {@link NormalizeOwnerTelephone} (which normalizes the incoming request)
 * and {@link EnsureUniqueOwnerTelephone} (which normalizes stored owners for comparison)
 * call this, so the create-time rule and the dedup comparison can never silently diverge.
 */
final class OwnerTelephones {

    private OwnerTelephones() {
    }

    /** Returns {@code telephone} with every non-digit character removed ("" when null). */
    static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
