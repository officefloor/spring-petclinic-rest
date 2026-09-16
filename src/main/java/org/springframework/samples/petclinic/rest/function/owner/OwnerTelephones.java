package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single normalization rule for owner telephones: strip every non-digit character.
 * Shared by {@link NormalizeOwnerTelephone} (which additionally enforces the digit count)
 * and {@link RejectDuplicateOwnerTelephone} so both compare telephones the same way.
 */
final class OwnerTelephones {

    private OwnerTelephones() {
    }

    /** The digits of {@code telephone}, or the empty string when it is {@code null}. */
    static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
