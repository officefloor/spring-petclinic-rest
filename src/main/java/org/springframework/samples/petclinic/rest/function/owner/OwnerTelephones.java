package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Shared telephone normalization for the create-owner pipeline: reduces a telephone to
 * its digits so validation, uniqueness checks and storage all compare the same value.
 */
final class OwnerTelephones {

    private OwnerTelephones() {
    }

    /** The digits of {@code telephone} (every non-digit stripped); {@code ""} when null. */
    static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
