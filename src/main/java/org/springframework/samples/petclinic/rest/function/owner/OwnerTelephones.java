package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for what a "normalized" owner telephone is: every non-digit
 * character stripped. Used by {@link NormalizeOwnerTelephone} when storing and by
 * {@link EnsureUniqueOwnerTelephone} when comparing against existing owners, so the rule
 * cannot drift between the two steps.
 */
final class OwnerTelephones {

    private OwnerTelephones() {
    }

    static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
