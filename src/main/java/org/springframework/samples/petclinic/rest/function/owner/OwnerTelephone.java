package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for the owner telephone normalization rule, shared by the
 * owner-create pipeline. The uniqueness check only holds if both the value stored for a
 * new owner and the values compared against existing owners are normalized identically,
 * so that rule lives here rather than being duplicated across steps.
 */
final class OwnerTelephone {

    private OwnerTelephone() {
    }

    /** Normalize a telephone to its digits, or {@code null} if the input is {@code null}. */
    static String normalize(String telephone) {
        return telephone == null ? null : telephone.replaceAll("\\D", "");
    }
}
