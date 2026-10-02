package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for what a "normalized" owner telephone is: the digits-only form, with every
 * non-digit character stripped. Both {@link ValidateNewOwner} (which enforces the 10-digit rule and
 * stores the normalized value) and {@link CheckTelephoneUnique} (which compares against stored owners)
 * normalize through here, so the two cannot drift apart.
 */
final class OwnerTelephone {

    private OwnerTelephone() {
    }

    static String normalize(String telephone) {
        return telephone.replaceAll("\\D", "");
    }
}
